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
import com.ssafy.cheket.service.ipfs.PinataService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ShowMintingServiceImpl — EventNFT + TicketNFT 배치 발행 구현체
 *
 * [기획서 7.1 — 2단계: 예매 오픈 D-1] 예매 시작 전날에 스케줄러가 자동 실행하여: ① EventNFT 발행 (공연 정보 +
 * StakeholderNFT 참조 + 예매 규칙 온체인 확정) ② EventNFT.addSession() (회차별 등록) ③
 * EventNFT.setRefundPolicy() (환불 정책 온체인 확정) ④ TicketNFT.batchMintTickets()
 * (100개씩 배치 발행, 플랫폼 지갑 소유)
 *
 * [왜 D-1에 발행하는가?] - D-1까지는 공연 정보를 수정할 수 있음 (좌석 가격, 예매 기간 등) - 온체인에 기록된 후에는 변경
 * 불가 → 투명성 확보 - 예매 오픈 전에 전체 TicketNFT가 존재해야 totalSupply 검증 가능
 *
 * [상태 전이] DRAFT → MINTING → MINTED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShowMintingServiceImpl implements ShowMintingService {

    private final BlockchainService blockchainService;
    private final PinataService pinataService;
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

    // 민팅 실패 시 최대 재시도 횟수
    private static final int MAX_RETRY = 5;

    // ========== 일괄 민팅 (수동 API용) ==========

    @Override
    public Map<String, Object> mintAllDraftShows() {
        LocalDateTime deadline = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);
        List<Show> draftShows = showRepository.findByStatusAndReservationStartDateBetween(ShowStatus.DRAFT,
            LocalDateTime.MIN, deadline);

        log.info("[민팅] 예매 시작일 ≤ {} 인 DRAFT 공연 민팅 — {}건", deadline.toLocalDate(), draftShows.size());

        int success = 0;
        int failed = 0;

        for (Show show : draftShows) {
            try {
                mintShowNfts(show.getId());
                success++;
            } catch (Exception e) {
                log.error("[민팅] 공연 {} 민팅 실패", show.getId(), e);
                failed++;
            }
        }

        return Map.of("message", "전체 민팅 완료", "total", draftShows.size(), "success", success, "failed", failed);
    }

    // ========== 스케줄러용 — 예매 오픈 임박 DRAFT 공연 자동 민팅 ==========

    @Override
    public int mintPendingDraftShows() {
        // 내일 23:59:59까지 — 예매 시작일이 이 시점 이하인 DRAFT 공연
        LocalDateTime deadline = LocalDate.now().plusDays(1).atTime(LocalTime.MAX);

        List<Show> showsToMint = showRepository.findByStatusAndReservationStartDateBetween(ShowStatus.DRAFT,
            LocalDateTime.MIN, deadline);

        if (showsToMint.isEmpty()) {
            log.info("[민팅] 민팅 대상 DRAFT 공연 없음");
            return 0;
        }

        log.info("[민팅] 예매 시작일 ≤ {} 인 DRAFT 공연 {}건 발견", deadline.toLocalDate(), showsToMint.size());

        // 순차 처리 (플랫폼 지갑 1개 → 병렬하면 Nonce 충돌)
        for (Show show : showsToMint) {
            mintWithRetry(show);
        }

        return showsToMint.size();
    }

    /**
     * 공연 1건 민팅 + 실패 시 즉시 재시도 (최대 MAX_RETRY회)
     *
     * [왜 재시도?] 블록체인 TX는 네트워크 상태에 따라 간헐적으로 실패 가능 (가스 부족, 타임아웃, RPC 연결 끊김 등) 대부분 즉시
     * 재시도하면 성공함
     */
    private void mintWithRetry(Show show) {
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                log.info("[민팅] 공연 '{}' (ID:{}) 민팅 시도 {}/{}", show.getTitle(), show.getId(), attempt, MAX_RETRY);

                mintShowNfts(show.getId());

                log.info("[민팅] 공연 '{}' (ID:{}) 민팅 완료", show.getTitle(), show.getId());
                return;
            } catch (Exception e) {
                if (attempt == MAX_RETRY) {
                    log.error("[민팅] 공연 '{}' (ID:{}) {}회 모두 실패" + " — 관리자 확인 필요", show.getTitle(), show.getId(),
                        MAX_RETRY, e);
                } else {
                    log.warn("[민팅] 공연 '{}' (ID:{}) {}회차 실패," + " 즉시 재시도", show.getTitle(), show.getId(), attempt, e);
                }
            }
        }
    }

    // ========== 단건 민팅 ==========

    @Override
    @Transactional
    public Map<String, Object> mintShowNfts(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new BlockchainException("공연을 찾을 수 없습니다: " + showId));

        // Lazy 프록시 강제 초기화 — IPFS 업로드(외부 HTTP) 중 세션 끊김 방지
        if (show.getVenue() != null) {
            show.getVenue().getName();
        }

        // ========== ① DRAFT → MINTING (원자적, 중복 요청 방지) ==========
        int updated = showRepository.updateStatusToMintingIfDraft(showId);
        if (updated == 0) {
            log.warn("공연 {}는 DRAFT 상태가 아닙니다 (현재: {}) — 중복 요청 무시", showId, show.getStatus());
            return Map.of("message", "이미 민팅되었거나 진행 중입니다", "showId", showId, "status", show.getStatus().name());
        }
        show.setStatus(ShowStatus.MINTING); // 엔티티 캐시 동기화
        log.info("[공연 {}] DRAFT → MINTING 상태 전이", showId);

        List<String> txHashes = new java.util.ArrayList<>();

        try {
            // ========== ② 포스터 + 메타데이터 IPFS 업로드 ==========
            uploadToIpfs(show);

            // ========== ③ EventNFT 발행 ==========
            BigInteger onChainEventId = mintEventNft(show, txHashes);

            // ========== ④ 회차별 addSession ==========
            List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);
            for (Session session : sessions) {
                addSessionOnChain(session, onChainEventId, txHashes);
            }

            // ========== ⑤ 환불 정책 온체인 등록 ==========
            setRefundPolicyOnChain(show, onChainEventId);

            // ========== ⑥ TicketNFT 배치 발행 ==========
            for (Session session : sessions) {
                batchMintTicketsForSession(show, session, onChainEventId, txHashes);
            }

            // ========== ⑦ 이벤트 단위 tokenURI 설정 (공연당 1번) ==========
            // 기존: setTicketTokenURI(tokenId) × 좌석 수 → TX 500개 ≈ 17분
            // 변경: setEventTokenURI(eventId) × 1 → TX 1개 ≈ 2초
            if (show.getMetadataIpfsCid() != null && !show.getMetadataIpfsCid().isEmpty()) {
                final BigInteger finalEventId = onChainEventId;
                TransactionReceipt uriReceipt = blockchainService.executePlatformTx(() -> blockchainService
                    .getTicketNFT().setEventTokenURI(finalEventId, "ipfs://" + show.getMetadataIpfsCid()).send());
                txHashes.add(uriReceipt.getTransactionHash());
                log.info("[공연 {}] 이벤트 tokenURI 설정 완료 — eventId={}", show.getId(), onChainEventId);
            }

            // ========== ⑧ MINTING → MINTED ==========
            show.setStatus(ShowStatus.MINTED);
            showRepository.save(show);
            log.info("[공연 {}] MINTING → MINTED 상태 전이 완료", showId);

            return Map.of("message", "민팅 완료", "showId", showId, "eventNftId", show.getEventNftId(), "txHashes",
                txHashes);

        } catch (Exception e) {
            // 실패 시 MINTING → DRAFT로 복원 (재시도 가능하도록)
            show.setStatus(ShowStatus.DRAFT);
            showRepository.save(show);
            log.error("[공연 {}] 민팅 실패, DRAFT로 복원", showId, e);
            throw new BlockchainException("공연 민팅 실패: " + e.getMessage());
        }
    }

    // ========== private 메서드 ==========

    /**
     * 포스터 이미지 + 메타데이터 JSON을 IPFS에 업로드
     *
     * [처리 흐름] ① S3에 있는 포스터 이미지 → Pinata로 IPFS 업로드 → posterIpfsCid 저장 ② 공연 메타데이터
     * JSON 생성 (NFT 표준 형식) → IPFS 업로드 → metadataIpfsCid 저장 ③ metadataIpfsCid는 이후
     * EventNFT.createEvent()에 전달되어 온체인에 영구 기록
     *
     * [이미 업로드된 경우] metadataIpfsCid가 이미 있으면 스킵 (재시도 시 중복 업로드 방지)
     */
    private void uploadToIpfs(Show show) {
        // 이미 IPFS 업로드 완료된 경우 스킵 (민팅 재시도 시 중복 방지)
        if (show.getMetadataIpfsCid() != null && !show.getMetadataIpfsCid().isEmpty()) {
            log.info("[공연 {}] IPFS 이미 업로드됨 — metadataCID={}", show.getId(), show.getMetadataIpfsCid());
            return;
        }

        try {
            // ① 포스터 이미지 → IPFS 업로드
            String posterCid = null;
            if (show.getPosterUrl() != null && !show.getPosterUrl().isEmpty()) {
                posterCid = pinataService.uploadFileFromUrl(show.getPosterUrl(), "show_" + show.getId() + "_poster");
                show.setPosterIpfsCid(posterCid);
                log.info("[공연 {}] 포스터 IPFS 업로드 완료 — CID={}", show.getId(), posterCid);
            }

            // ② 메타데이터 JSON → IPFS 업로드 (ERC-721 표준 형식)
            Map<String, Object> metadata = new java.util.LinkedHashMap<>();
            metadata.put("name", show.getTitle());
            metadata.put("description", show.getDescription() != null ? show.getDescription() : "");
            metadata.put("image", posterCid != null ? pinataService.getIpfsUri(posterCid) : "");

            // Venue는 LAZY 프록시 — 세션이 닫힌 경우 대비하여 안전하게 조회
            String venueName = "";
            try {
                if (show.getVenue() != null) {
                    venueName = show.getVenue().getName();
                }
            } catch (Exception ex) {
                log.debug("[공연 {}] Venue 로딩 실패 (Lazy 프록시) — 빈 문자열로 대체", show.getId());
            }

            Map<String, Object> attributes = new java.util.LinkedHashMap<>();
            attributes.put("artist", show.getArtist() != null ? show.getArtist() : "");
            attributes.put("venue", venueName);
            attributes.put("showStartDate", show.getShowStartDate() != null ? show.getShowStartDate().toString() : "");
            attributes.put("showEndDate", show.getShowEndDate() != null ? show.getShowEndDate().toString() : "");
            attributes.put("playtime", show.getPlaytime() != null ? show.getPlaytime() : 0);
            metadata.put("attributes", attributes);

            String metadataCid = pinataService.uploadJson(metadata, "show_" + show.getId() + "_metadata");
            show.setMetadataIpfsCid(metadataCid);
            showRepository.save(show);

            log.info("[공연 {}] 메타데이터 IPFS 업로드 완료 — CID={}", show.getId(), metadataCid);

        } catch (Exception e) {
            // IPFS 업로드 실패해도 민팅은 진행 (메타데이터는 선택적)
            log.warn("[공연 {}] IPFS 업로드 실패 — 빈 metadataCID로 민팅 진행: {}", show.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public Map<String, Object> reuploadIpfs(Long showId) {
        Show show = showRepository.findById(showId)
            .orElseThrow(() -> new BlockchainException("공연을 찾을 수 없습니다: " + showId));

        // Lazy 프록시 강제 초기화
        if (show.getVenue() != null) {
            show.getVenue().getName();
        }

        // 기존 metadataIpfsCid 초기화하여 재업로드 허용
        show.setMetadataIpfsCid(null);
        show.setPosterIpfsCid(null);

        uploadToIpfs(show);

        return Map.of("showId", showId, "posterIpfsCid",
            show.getPosterIpfsCid() != null ? show.getPosterIpfsCid() : "실패", "metadataIpfsCid",
            show.getMetadataIpfsCid() != null ? show.getMetadataIpfsCid() : "실패");
    }

    private BigInteger mintEventNft(Show show, List<String> txHashes) throws Exception {
        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(show.getId());
        List<BigInteger> stakeholderTokenIds = stakeholders.stream()
            .map(s -> BigInteger.valueOf(s.getStakeholderNftId())).collect(Collectors.toList());

        int totalSupply = sessionSeatRepository.countTotalSeatsByShowId(show.getId());

        String metadataCID = show.getMetadataIpfsCid() != null ? show.getMetadataIpfsCid() : "";

        BigInteger bookingStart = toBigIntTimestamp(show.getReservationStartDate());
        BigInteger bookingEnd = toBigIntTimestamp(show.getReservationEndDate());

        log.info("[공연 {}] EventNFT 발행 시작 — totalSupply={}," + " maxPerWallet={}, stakeholders={}", show.getId(),
            totalSupply, show.getPurchaseLimit(), stakeholderTokenIds.size());

        TransactionReceipt receipt = blockchainService.executePlatformTx(() -> blockchainService.getEventNFT()
            .createEvent(stakeholderTokenIds, metadataCID, BigInteger.valueOf(totalSupply),
                BigInteger.valueOf(show.getPurchaseLimit()), BigInteger.valueOf(10000), bookingStart, bookingEnd)
            .send());

        List<EventNFT.EventCreatedEventResponse> events = blockchainService.getEventNFT()
            .getEventCreatedEvents(receipt);

        if (events.isEmpty()) {
            throw new BlockchainException("EventCreated 이벤트를 찾을 수 없습니다");
        }

        BigInteger onChainEventId = events.get(0).eventId;

        show.setEventNftId(onChainEventId.longValue());
        showRepository.save(show);

        txHashes.add(receipt.getTransactionHash());
        log.info("[공연 {}] EventNFT 발행 완료 — onChainEventId={}," + " txHash={}", show.getId(), onChainEventId,
            receipt.getTransactionHash());

        return onChainEventId;
    }

    private void addSessionOnChain(Session session, BigInteger onChainEventId, List<String> txHashes) throws Exception {

        List<SessionSeat> seats = sessionSeatRepository.findBySessionId(session.getId());
        int ticketSupply = seats.size();

        BigInteger sessionTimestamp = toBigIntTimestamp(session.getSessionDate());

        log.info("[회차 {}] addSession 시작 — ticketSupply={}", session.getId(), ticketSupply);

        TransactionReceipt receipt = blockchainService.executePlatformTx(() -> blockchainService.getEventNFT()
            .addSession(onChainEventId, sessionTimestamp, BigInteger.valueOf(ticketSupply)).send());

        List<EventNFT.SessionAddedEventResponse> events = blockchainService.getEventNFT()
            .getSessionAddedEvents(receipt);

        if (!events.isEmpty()) {
            BigInteger onChainSessionId = events.get(0).sessionId;
            session.setOnChainSessionId(onChainSessionId.longValue());
            sessionRepository.save(session);

            txHashes.add(receipt.getTransactionHash());
            log.info("[회차 {}] addSession 완료 — onChainSessionId={}", session.getId(), onChainSessionId);
        }
    }

    private void setRefundPolicyOnChain(Show show, BigInteger onChainEventId) throws Exception {

        List<RefundPolicy> policies = refundPolicyRepository.findByShowIdOrderByDaysRemainingDesc(show.getId());

        if (policies.isEmpty()) {
            log.warn("[공연 {}] 환불 정책이 없어 온체인 등록 생략", show.getId());
            return;
        }

        List<BigInteger> daysArray = new ArrayList<>();
        List<BigInteger> rateBpsArray = new ArrayList<>();

        for (RefundPolicy policy : policies) {
            daysArray.add(BigInteger.valueOf(policy.getDaysRemaining()));
            rateBpsArray.add(BigInteger.valueOf(policy.getRefundRate() * 100L));
        }

        log.info("[공연 {}] 환불 정책 온체인 등록 — {}건", show.getId(), policies.size());

        blockchainService.executePlatformTx(() -> {
            blockchainService.getEventNFT().setRefundPolicy(onChainEventId, daysArray, rateBpsArray).send();
            return null;
        });

        log.info("[공연 {}] 환불 정책 온체인 등록 완료", show.getId());
    }

    private void batchMintTicketsForSession(Show show, Session session, BigInteger onChainEventId,
        List<String> txHashes) throws Exception {

        if (session.getOnChainSessionId() == null) {
            throw new BlockchainException("회차 " + session.getId() + "의 onChainSessionId가 없습니다");
        }

        BigInteger onChainSessionId = BigInteger.valueOf(session.getOnChainSessionId());
        String platformAddress = blockchainService.getPlatformAddress();

        List<SessionSeat> sessionSeats = sessionSeatRepository.findBySessionId(session.getId());

        List<Long> seatIds = sessionSeats.stream().map(SessionSeat::getSeatId).collect(Collectors.toList());
        Map<Long, Seat> seatMap = seatRepository.findAllById(seatIds).stream()
            .collect(Collectors.toMap(Seat::getId, s -> s));

        List<Long> sectionIds = seatMap.values().stream().map(Seat::getSectionId).distinct()
            .collect(Collectors.toList());
        Map<Long, Section> sectionMap = sectionRepository.findAllById(sectionIds).stream()
            .collect(Collectors.toMap(Section::getId, s -> s));

        List<SeatGrade> seatGrades = seatGradeRepository.findByShowId(show.getId());
        Map<Long, SeatGrade> gradeMap = seatGrades.stream().collect(Collectors.toMap(SeatGrade::getSectionId, g -> g));

        Map<String, List<SessionSeat>> groups = sessionSeats.stream().collect(Collectors.groupingBy(ss -> {
            Seat seat = seatMap.get(ss.getSeatId());
            return seat.getSectionId() + ":" + seat.getRowNum();
        }));

        int totalMinted = 0;

        for (Map.Entry<String, List<SessionSeat>> entry : groups.entrySet()) {

            List<SessionSeat> groupSeats = entry.getValue();

            Seat firstSeat = seatMap.get(groupSeats.get(0).getSeatId());
            Section section = sectionMap.get(firstSeat.getSectionId());
            SeatGrade grade = gradeMap.get(firstSeat.getSectionId());

            if (grade == null) {
                log.warn("[공연 {}] 구역 {}의 등급 정보가 없어 스킵", show.getId(), section.getSectionName());
                continue;
            }

            groupSeats.sort((a, b) -> {
                Seat sa = seatMap.get(a.getSeatId());
                Seat sb = seatMap.get(b.getSeatId());
                return Integer.compare(sa.getColNum(), sb.getColNum());
            });

            for (int i = 0; i < groupSeats.size(); i += BATCH_SIZE) {

                int end = Math.min(i + BATCH_SIZE, groupSeats.size());
                List<SessionSeat> batch = groupSeats.subList(i, end);

                Seat startSeat = seatMap.get(batch.get(0).getSeatId());
                int startSeatNum = startSeat.getColNum();
                int count = batch.size();

                log.info("[공연 {}][회차 {}] 배치 발행: {} {}열 {}번~{}번 ({}개)", show.getId(), session.getId(),
                    section.getSectionName(), firstSeat.getRowNum(), startSeatNum, startSeatNum + count - 1, count);

                TransactionReceipt receipt = blockchainService.executePlatformTx(() -> blockchainService.getTicketNFT()
                    .batchMintTickets(platformAddress, onChainEventId, onChainSessionId, section.getSectionName(),
                        BigInteger.valueOf(firstSeat.getRowNum()), BigInteger.valueOf(startSeatNum),
                        BigInteger.valueOf(count), grade.getGradeName(), BigInteger.valueOf(grade.getPrice()))
                    .send());

                List<TicketNFT.BatchTicketMintedEventResponse> mintEvents = blockchainService.getTicketNFT()
                    .getBatchTicketMintedEvents(receipt);

                txHashes.add(receipt.getTransactionHash());
                if (!mintEvents.isEmpty()) {
                    BigInteger startTokenId = mintEvents.get(0).startTokenId;

                    for (int j = 0; j < batch.size(); j++) {
                        SessionSeat ss = batch.get(j);
                        long tokenId = startTokenId.longValue() + j;
                        ss.setOnChainTicketNftId(tokenId);
                    }
                    sessionSeatRepository.saveAll(batch);
                }

                totalMinted += count;
            }
        }

        log.info("[공연 {}][회차 {}] 배치 발행 완료 — 총 {}개" + " TicketNFT 발행", show.getId(), session.getId(), totalMinted);
    }

    private BigInteger toBigIntTimestamp(LocalDateTime dateTime) {
        return BigInteger.valueOf(dateTime.atZone(ZoneId.of("Asia/Seoul")).toEpochSecond());
    }
}
