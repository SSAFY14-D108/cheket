package com.ssafy.cheket.service.blockchain;

import com.ssafy.cheket.blockchain.contract.EventNFT;
import com.ssafy.cheket.blockchain.contract.TicketNFT;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.RefundPolicy;
import com.ssafy.cheket.entity.show.Seat;
import com.ssafy.cheket.entity.show.SeatGrade;
import com.ssafy.cheket.entity.show.Section;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.exception.common.BlockchainException;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.RefundPolicyRepository;
import com.ssafy.cheket.repository.show.SeatGradeRepository;
import com.ssafy.cheket.repository.show.SeatRepository;
import com.ssafy.cheket.repository.show.SectionRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ShowMintingService — EventNFT + TicketNFT 배치 발행 서비스
 *
 * [기획서 7.1 — 2단계: 예매 오픈 D-1] 예매 시작 전날에 스케줄러가 자동 실행하여: ① EventNFT 발행 (공연 정보 +
 * StakeholderNFT 참조 + 예매 규칙 온체인 확정) ② EventNFT.addSession() (회차별 등록) ③
 * EventNFT.setRefundPolicy() (환불 정책 온체인 확정) ④ TicketNFT.batchMintTickets()
 * (100개씩 배치 발행, 플랫폼 지갑 소유)
 *
 * [왜 D-1에 발행하는가?] - D-1까지는 공연 정보를 수정할 수 있음 (좌석 가격, 예매 기간 등) - 온체인에 기록된 후에는 변경
 * 불가 → 투명성 확보 - 예매 오픈 전에 전체 TicketNFT가 존재해야 totalSupply 검증 가능
 *
 * [상태 전이] DRAFT → MINTING → MINTED DRAFT: 공연 정보 입력 완료, 아직 온체인 미등록 MINTING:
 * EventNFT + TicketNFT 발행 중 (중간 상태) MINTED: 발행 완료, 예매 가능 상태
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShowMintingService {

    private final BlockchainService blockchainService;
    private final ShowRepository showRepository;
    private final SessionRepository sessionRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final SectionRepository sectionRepository;
    private final StakeholderRepository stakeholderRepository;
    private final RefundPolicyRepository refundPolicyRepository;

    // TicketNFT 배치 발행 시 한 TX당 최대 좌석 수
    // 100개 이상이면 block gas limit 초과 위험
    private static final int BATCH_SIZE = 100;

    /**
     * 공연 1건에 대해 EventNFT + TicketNFT 전체 발행
     *
     * [실행 흐름] ① DRAFT → MINTING 상태 전이 ② EventNFT.createEvent() — 공연 정보 온체인 등록,
     * onChainEventId 추출 → DB 저장 ③ EventNFT.addSession() × N — 회차별 등록,
     * onChainSessionId 추출 → DB 저장 ④ EventNFT.setRefundPolicy() — 환불 정책 온체인 등록 ⑤
     * TicketNFT.batchMintTickets() × M — 100개씩 배치 발행 ⑥ MINTING → MINTED 상태 전이
     *
     * @Transactional: 어느 단계에서든 실패하면 DB 전체 롤백 단, 이미 온체인에 기록된 TX는 롤백 불가 (블록체인 특성)
     */
    @Transactional
    public void mintShowNfts(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new BlockchainException("공연을 찾을 수 없습니다: " + showId));

        // 이미 발행됐거나 발행 중이면 스킵
        if (show.getStatus() != ShowStatus.DRAFT) {
            log.warn("공연 {}는 DRAFT 상태가 아닙니다 (현재: {})", showId, show.getStatus());
            return;
        }

        // ========== ① DRAFT → MINTING ==========
        show.setStatus(ShowStatus.MINTING);
        showRepository.save(show);
        log.info("[공연 {}] DRAFT → MINTING 상태 전이", showId);

        try {
            // ========== ② EventNFT 발행 ==========
            BigInteger onChainEventId = mintEventNft(show);

            // ========== ③ 회차별 addSession ==========
            List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);
            for (Session session : sessions) {
                addSessionOnChain(session, onChainEventId);
            }

            // ========== ④ 환불 정책 온체인 등록 ==========
            setRefundPolicyOnChain(show, onChainEventId);

            // ========== ⑤ TicketNFT 배치 발행 ==========
            for (Session session : sessions) {
                batchMintTicketsForSession(show, session, onChainEventId);
            }

            // ========== ⑥ MINTING → MINTED ==========
            show.setStatus(ShowStatus.MINTED);
            showRepository.save(show);
            log.info("[공연 {}] MINTING → MINTED 상태 전이 완료", showId);

        } catch (Exception e) {
            // 실패 시 MINTING → DRAFT로 복원 (재시도 가능하도록)
            show.setStatus(ShowStatus.DRAFT);
            showRepository.save(show);
            log.error("[공연 {}] 민팅 실패, DRAFT로 복원", showId, e);
            throw new BlockchainException("공연 민팅 실패: " + e.getMessage());
        }
    }

    /**
     * ② EventNFT.createEvent() — 공연 정보 온체인 등록
     *
     * [온체인에 기록되는 것] - stakeholderTokenIds: StakeholderNFT ID 배열 (수익 분배 참조) -
     * metadataCID: IPFS 메타데이터 CID (포스터/설명) - totalSupply: 총 좌석 수 (모든 회차 합산) -
     * maxPerWallet: 1인당 최대 구매 수 - resaleCapBps: 리세일 가격 상한 (10000 = 원가 100%) -
     * bookingStartTime/EndTime: 예매 기간 (unix timestamp)
     *
     * [기획서 핵심] "예매 오픈 전 전체 TicketNFT가 온체인에 발행된다. 누구나 totalSupply를 조회하여 실제 발행 수량과
     * 좌석별 가격을 독립 검증할 수 있다 — 발행 수량 불투명 문제를 원천 차단"
     */
    private BigInteger mintEventNft(Show show) throws Exception {
        // StakeholderNFT ID 목록 조회 (2번 단계에서 이미 발행된 것)
        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(show.getId());
        List<BigInteger> stakeholderTokenIds = stakeholders.stream()
            .map(s -> BigInteger.valueOf(s.getStakeholderNftId())).collect(Collectors.toList());

        // 총 좌석 수 계산 (모든 회차 합산)
        int totalSupply = sessionSeatRepository.countTotalSeatsByShowId(show.getId());

        // IPFS CID (공연 등록 시 업로드된 메타데이터)
        // null이면 빈 문자열 (IPFS 미사용 시)
        String metadataCID = show.getMetadataIpfsCid() != null ? show.getMetadataIpfsCid() : "";

        // LocalDateTime → unix timestamp 변환 (블록체인은 초 단위 unix timestamp 사용)
        BigInteger bookingStart = toBigIntTimestamp(show.getReservationStartDate());
        BigInteger bookingEnd = toBigIntTimestamp(show.getReservationEndDate());

        log.info("[공연 {}] EventNFT 발행 시작 — totalSupply={}, maxPerWallet={}, stakeholders={}", show.getId(), totalSupply,
            show.getPurchaseLimit(), stakeholderTokenIds.size());

        // 온체인 TX 전송 — createEvent()
        TransactionReceipt receipt = blockchainService.getEventNFT()
            .createEvent(stakeholderTokenIds, metadataCID, BigInteger.valueOf(totalSupply),
                BigInteger.valueOf(show.getPurchaseLimit()), BigInteger.valueOf(10000), // resaleCapBps: 원가 100%까지 리세일
                                                                                        // 허용
                bookingStart, bookingEnd)
            .send();

        // TransactionReceipt에서 EventCreated 이벤트 파싱 → eventId 추출
        List<EventNFT.EventCreatedEventResponse> events = blockchainService.getEventNFT()
            .getEventCreatedEvents(receipt);

        if (events.isEmpty()) {
            throw new BlockchainException("EventCreated 이벤트를 찾을 수 없습니다");
        }

        BigInteger onChainEventId = events.get(0).eventId;

        // DB에 onChainEventId 저장 (온체인 ↔ 오프체인 매핑)
        show.setEventNftId(onChainEventId.longValue());
        showRepository.save(show);

        log.info("[공연 {}] EventNFT 발행 완료 — onChainEventId={}, txHash={}", show.getId(), onChainEventId,
            receipt.getTransactionHash());

        return onChainEventId;
    }

    /**
     * ③ EventNFT.addSession() — 회차 온체인 등록
     *
     * [온체인에 기록되는 것] - eventId: 이 회차가 속한 EventNFT ID - sessionTimestamp: 공연 일시 (unix
     * timestamp) - ticketSupply: 이 회차의 좌석 수
     *
     * [다회차 공연 예시] 10/5 공연 → Session #0 (250석) 10/6 공연 → Session #1 (250석) → 각각
     * 독립적으로 정산 가능 (기획서 7.5)
     */
    private void addSessionOnChain(Session session, BigInteger onChainEventId) throws Exception {
        // 이 회차의 좌석 수 계산
        List<SessionSeat> seats = sessionSeatRepository.findBySessionId(session.getId());
        int ticketSupply = seats.size();

        // 공연 일시 → unix timestamp
        BigInteger sessionTimestamp = toBigIntTimestamp(session.getSessionDate());

        log.info("[회차 {}] addSession 시작 — ticketSupply={}", session.getId(), ticketSupply);

        // 온체인 TX 전송 — addSession()
        TransactionReceipt receipt = blockchainService.getEventNFT()
            .addSession(onChainEventId, sessionTimestamp, BigInteger.valueOf(ticketSupply)).send();

        // SessionAdded 이벤트에서 onChainSessionId 추출
        List<EventNFT.SessionAddedEventResponse> events = blockchainService.getEventNFT()
            .getSessionAddedEvents(receipt);

        if (!events.isEmpty()) {
            BigInteger onChainSessionId = events.get(0).sessionId;
            session.setOnChainSessionId(onChainSessionId.longValue());
            sessionRepository.save(session);

            log.info("[회차 {}] addSession 완료 — onChainSessionId={}", session.getId(), onChainSessionId);
        }
    }

    /**
     * ④ EventNFT.setRefundPolicy() — 환불 정책 온체인 등록
     *
     * [온체인에 기록되는 것] - daysArray: 남은 일수 배열 (내림차순: 7, 3, 1) - rateBpsArray: 환불 비율 배열
     * (10000, 5000, 0)
     *
     * [기획서 핵심 — 왜 온체인?] 백엔드가 환불액을 계산하면 → 조작 가능 컨트랙트가 on-chain에서 직접 계산하면 → 조작 불가
     * (투명성) Settlement.refund()가 이 정책을 읽어서 환불액 자동 계산
     */
    private void setRefundPolicyOnChain(Show show, BigInteger onChainEventId) throws Exception {
        List<RefundPolicy> policies = refundPolicyRepository.findByShowIdOrderByDaysRemainingDesc(show.getId());

        if (policies.isEmpty()) {
            log.warn("[공연 {}] 환불 정책이 없어 온체인 등록 생략", show.getId());
            return;
        }

        // DB의 RefundPolicy → 온체인 파라미터 변환
        List<BigInteger> daysArray = new ArrayList<>();
        List<BigInteger> rateBpsArray = new ArrayList<>();

        for (RefundPolicy policy : policies) {
            daysArray.add(BigInteger.valueOf(policy.getDaysRemaining()));
            rateBpsArray.add(BigInteger.valueOf(policy.getRefundRate()));
        }

        log.info("[공연 {}] 환불 정책 온체인 등록 — {}건", show.getId(), policies.size());

        blockchainService.getEventNFT().setRefundPolicy(onChainEventId, daysArray, rateBpsArray).send();

        log.info("[공연 {}] 환불 정책 온체인 등록 완료", show.getId());
    }

    /**
     * ⑤ TicketNFT.batchMintTickets() — 회차별 티켓 배치 발행
     *
     * [기획서 7.2 — Batch Minting] "5,000석 공연 — 예매 오픈 D-1 자동 실행 → 100개씩 배치 분할 → TX 큐에
     * 순차 투입 → TX 50개로 감소 (99% 감소)"
     *
     * [배치 발행 전략] 같은 구역 + 같은 열 + 같은 등급의 연속 좌석을 한 TX로 묶음 batchMintTickets(플랫폼,
     * eventId, sessionId, "A구역", 1열, 1번, 20개, "VIP", 50000) → A구역 1열 1~20번 좌석이 1
     * TX로 발행
     *
     * [소유권] 모든 TicketNFT는 플랫폼 지갑 소유로 발행 구매 시 PurchaseRouter가 transferFrom(플랫폼 →
     * 구매자)으로 이전 → mint보다 가스 적고 실패율 낮음
     */
    private void batchMintTicketsForSession(Show show, Session session, BigInteger onChainEventId) throws Exception {
        // onChainSessionId가 없으면 addSession이 실패한 것
        if (session.getOnChainSessionId() == null) {
            throw new BlockchainException("회차 " + session.getId() + "의 onChainSessionId가 없습니다");
        }

        BigInteger onChainSessionId = BigInteger.valueOf(session.getOnChainSessionId());
        String platformAddress = blockchainService.getPlatformAddress();

        // 이 회차의 모든 SessionSeat 조회
        List<SessionSeat> sessionSeats = sessionSeatRepository.findBySessionId(session.getId());

        // seatId → Seat 엔티티 매핑 (조회 최적화)
        List<Long> seatIds = sessionSeats.stream().map(SessionSeat::getSeatId).collect(Collectors.toList());
        Map<Long, Seat> seatMap = seatRepository.findAllById(seatIds).stream()
            .collect(Collectors.toMap(Seat::getId, s -> s));

        // sectionId → Section 엔티티 매핑
        List<Long> sectionIds = seatMap.values().stream().map(Seat::getSectionId).distinct()
            .collect(Collectors.toList());
        Map<Long, Section> sectionMap = sectionRepository.findAllById(sectionIds).stream()
            .collect(Collectors.toMap(Section::getId, s -> s));

        // sectionId → SeatGrade 매핑 (이 공연의 구역별 등급/가격)
        List<SeatGrade> seatGrades = seatGradeRepository.findByShowId(show.getId());
        Map<Long, SeatGrade> gradeMap = seatGrades.stream().collect(Collectors.toMap(SeatGrade::getSectionId, g -> g));

        // ===== 좌석을 (구역 + 열 + 등급) 기준으로 그룹핑 =====
        // 같은 그룹의 연속 좌석은 1 TX로 배치 발행 가능
        //
        // 예: A구역 1열 VIP 좌석 20개 → 1 TX
        // A구역 2열 VIP 좌석 20개 → 1 TX
        // B구역 1열 R석 30개 → 1 TX
        Map<String, List<SessionSeat>> groups = sessionSeats.stream().collect(Collectors.groupingBy(ss -> {
            Seat seat = seatMap.get(ss.getSeatId());
            return seat.getSectionId() + ":" + seat.getRowNum();
        }));

        int totalMinted = 0;

        // 각 그룹(같은 구역+열)에 대해 배치 발행
        for (Map.Entry<String, List<SessionSeat>> entry : groups.entrySet()) {
            List<SessionSeat> groupSeats = entry.getValue();

            // 첫 번째 좌석에서 구역/열/등급 정보 추출
            Seat firstSeat = seatMap.get(groupSeats.get(0).getSeatId());
            Section section = sectionMap.get(firstSeat.getSectionId());
            SeatGrade grade = gradeMap.get(firstSeat.getSectionId());

            if (grade == null) {
                log.warn("[공연 {}] 구역 {}의 등급 정보가 없어 스킵", show.getId(), section.getSectionName());
                continue;
            }

            // 좌석 번호순 정렬
            groupSeats.sort((a, b) -> {
                Seat sa = seatMap.get(a.getSeatId());
                Seat sb = seatMap.get(b.getSeatId());
                return Integer.compare(sa.getColNum(), sb.getColNum());
            });

            // BATCH_SIZE(100)개씩 나눠서 발행
            for (int i = 0; i < groupSeats.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, groupSeats.size());
                List<SessionSeat> batch = groupSeats.subList(i, end);

                // 시작 좌석 번호 (colNum 기준)
                Seat startSeat = seatMap.get(batch.get(0).getSeatId());
                int startSeatNum = startSeat.getColNum();
                int count = batch.size();

                log.info("[공연 {}][회차 {}] 배치 발행: {}구역 {}열 {}번~{}번 ({}개)", show.getId(), session.getId(),
                    section.getSectionName(), firstSeat.getRowNum(), startSeatNum, startSeatNum + count - 1, count);

                // ===== 온체인 TX 전송 — batchMintTickets() =====
                TransactionReceipt receipt = blockchainService.getTicketNFT().batchMintTickets(platformAddress, // to:
                                                                                                                // 플랫폼
                                                                                                                // 지갑 소유
                    onChainEventId, // eventId
                    onChainSessionId, // sessionId
                    section.getSectionName(), // section: "A", "VIP"
                    BigInteger.valueOf(firstSeat.getRowNum()), // row: 열 번호
                    BigInteger.valueOf(startSeatNum), // startSeat: 시작 좌석
                    BigInteger.valueOf(count), // count: 발행 수량
                    grade.getGradeName(), // grade: "VIP", "R", "S"
                    BigInteger.valueOf(grade.getPrice()) // price: SSF 가격
                ).send();

                // BatchTicketMinted 이벤트에서 startTokenId 추출
                List<TicketNFT.BatchTicketMintedEventResponse> mintEvents = blockchainService.getTicketNFT()
                    .getBatchTicketMintedEvents(receipt);

                if (!mintEvents.isEmpty()) {
                    BigInteger startTokenId = mintEvents.get(0).startTokenId;

                    // 각 SessionSeat에 onChainTicketNftId 매핑
                    // startTokenId, startTokenId+1, startTokenId+2, ... 순서
                    for (int j = 0; j < batch.size(); j++) {
                        SessionSeat ss = batch.get(j);
                        ss.setOnChainTicketNftId(startTokenId.longValue() + j);
                    }
                    sessionSeatRepository.saveAll(batch);
                }

                totalMinted += count;
            }
        }

        log.info("[공연 {}][회차 {}] 배치 발행 완료 — 총 {}개 TicketNFT 발행", show.getId(), session.getId(), totalMinted);
    }

    /**
     * LocalDateTime → unix timestamp (BigInteger) 변환 블록체인의 block.timestamp는 초 단위
     * unix timestamp
     */
    private BigInteger toBigIntTimestamp(LocalDateTime dateTime) {
        return BigInteger.valueOf(dateTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond());
    }
}
