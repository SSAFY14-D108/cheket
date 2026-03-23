package com.ssafy.cheket.service.host;

import com.ssafy.cheket.config.s3.S3Uploader;
import com.ssafy.cheket.dto.host.response.CreateShowResponse;
import com.ssafy.cheket.dto.host.response.GetHostShowDetailResponse;
import com.ssafy.cheket.dto.show.request.AddShowRequest;
import com.ssafy.cheket.dto.show.request.UpdateShowRequest;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.ShowItem;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.entity.ticket.TicketEffect;
import com.ssafy.cheket.entity.transaction.Transaction;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.enums.ShowStatus;
import com.ssafy.cheket.enums.StakeholderRole;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.*;
import com.ssafy.cheket.repository.ticket.TicketEffectRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.repository.wallet.TransactionRepository;
import com.ssafy.cheket.service.blockchain.BlockchainAsyncWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Credentials;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostShowServiceImpl implements HostShowService {
    private final TicketEffectRepository ticketEffectRepository;
    private final ShowRepository showRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final StakeholderRepository stakeholderRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final SessionRepository sessionRepository;
    private final LikeRepository likeRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final HostRepository hostRepository;
    private final VenueRepository venueRepository;
    private final ShowImageRepository showImageRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final S3Uploader s3Uploader;
    private final BlockchainAsyncWorker blockchainAsyncWorker;
    // 클래스 상단에 필드 추가
    @Value("${blockchain.platform-private-key}")
    private String platformPrivateKey;

    // 티켓 효과 목록 조회
    @Override
    public List<GetTicketEffectsResponse> getTicketEffects() {
        List<TicketEffect> effects = ticketEffectRepository.findAll();

        return effects.stream().map(effect -> new GetTicketEffectsResponse(effect.getId(), effect.getEffect()))
            .toList();
    }

    // 내 공연 목록 조회
    @Override
    public GetShowListResponse<ShowItem> getMyShows(Long hostId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        Page<Show> result = showRepository.findByHost_id(hostId, pageable);
        List<ShowItem> items = result.getContent().stream().map(this::toShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    // 공연 상세 조회
    @Override
    public GetHostShowDetailResponse getHostShowDetail(Long hostId, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 조회할 수 있습니다.");

        List<SeatGrade> seatGrades = seatGradeRepository.findByShowId(showId);
        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(showId);
        List<RefundPolicy> refundPolicies = refundPolicyRepository.findByShowIdOrderByDaysRemainingDesc(showId);
        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);

        List<Long> sessionIds = sessions.stream().map(Session::getId).toList();

        Map<Long, Integer> capacityMap = sessionSeatRepository.countGroupedBySessionIds(sessionIds).stream()
            .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Long) row[1]).intValue()));

        List<Long> ticketEffectIds = seatGrades.stream().map(SeatGrade::getTicketEffectId).filter(Objects::nonNull)
            .distinct().toList();

        Map<Long, TicketEffect> ticketEffectMap = ticketEffectIds.isEmpty()
            ? Collections.emptyMap()
            : ticketEffectRepository.findAllById(ticketEffectIds).stream()
                .collect(Collectors.toMap(TicketEffect::getId, Function.identity()));

        List<Long> hostIds = stakeholders.stream()
            .filter(stakeholder -> stakeholder.getRole() == StakeholderRole.ORGANIZER).map(Stakeholder::getHostId)
            .filter(Objects::nonNull).distinct().toList();

        List<Long> userIds = stakeholders.stream()
            .filter(stakeholder -> stakeholder.getRole() == StakeholderRole.ARTIST).map(Stakeholder::getUserId)
            .filter(Objects::nonNull).distinct().toList();

        Map<Long, Host> hostMap = hostIds.isEmpty()
            ? Collections.emptyMap()
            : hostRepository.findAllById(hostIds).stream().collect(Collectors.toMap(Host::getId, Function.identity()));

        Map<Long, User> userMap = userIds.isEmpty()
            ? Collections.emptyMap()
            : userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<GetHostShowDetailResponse.StakeholderInfo> stakeholderInfos = stakeholders.stream().map(stakeholder -> {
            String name = null;
            String number = null;
            Long stakeholderId = null;

            if (stakeholder.getRole() == StakeholderRole.ORGANIZER) {
                stakeholderId = stakeholder.getHostId();

                Host host = hostMap.get(stakeholder.getHostId());
                if (host != null) {
                    name = host.getCompanyName();
                    number = host.getBusinessNo();
                }
            } else if (stakeholder.getRole() == StakeholderRole.ARTIST) {
                stakeholderId = stakeholder.getUserId();

                User user = userMap.get(stakeholder.getUserId());
                if (user != null) {
                    name = user.getUsername();
                    number = user.getPhoneNumber();
                }
            }

            return new GetHostShowDetailResponse.StakeholderInfo(stakeholder.getRole(), stakeholderId, name, number,
                stakeholder.getShareBps());
        }).toList();

        return new GetHostShowDetailResponse(show.getId(), show.getTitle(), show.getPosterUrl(),
            new GetHostShowDetailResponse.VenueInfo(show.getVenue().getId(), show.getVenue().getName(),
                show.getVenue().getAddress()),
            new GetHostShowDetailResponse.ShowPeriod(show.getShowStartDate().toLocalDate(),
                show.getShowEndDate().toLocalDate()),
            new GetHostShowDetailResponse.ReservationPeriod(show.getReservationStartDate(),
                show.getReservationEndDate()),
            show.getDescription(), show.getArtist(), show.getPlaytime(), show.getPurchaseLimit(),
            likeRepository.countByShowId(showId),

            seatGrades.stream().map(grade -> {
                TicketEffect ticketEffect = ticketEffectMap.get(grade.getTicketEffectId());

                return new GetHostShowDetailResponse.GradeInfo(grade.getSectionId(), grade.getGradeName(),
                    grade.getPrice(), grade.getColorCode(), grade.getTicketEffectId(),
                    ticketEffect != null ? ticketEffect.getEffect() : null);
            }).toList(),

            stakeholderInfos,

            refundPolicies.stream()
                .map(policy -> new GetHostShowDetailResponse.RefundPolicyInfo(policy.getDaysRemaining(),
                    policy.getRefundRate()))
                .toList(),

            sessions.stream()
                .map(session -> new GetHostShowDetailResponse.SessionInfo(session.getId(),
                    session.getSessionDate().toLocalDate(), session.getSessionStartTime().toLocalTime(),
                    capacityMap.getOrDefault(session.getId(), 0)))
                .toList(),
            show.getStatus(), show.getCreatedAt(), show.getUpdatedAt(),
            showImageRepository.findAllByShow_Id(showId).stream().map(ShowImage::getImageUrl).toList());
    }

    @Override
    @Transactional
    public CreateShowResponse createShow(Long hostId, AddShowRequest request, MultipartFile posterImage,
        List<MultipartFile> descriptionImages) {
        // 요청 데이터 검증
        int totalBps = request.stakeholders().stream().mapToInt(AddShowRequest.StakeholderInfo::shareBps).sum();

        if (totalBps + 800 != 10000) {
            // 10000 bps = 100%
            // 9000이면 → 9000 / 100 = 90 → "현재: 90%"
            throw new BadRequestException("수익 배분 합계가 100%가 아닙니다. (현재: " + totalBps / 100 + "%)");
        }

        // 0. 플랫폼 지갑 개인키에서 추출
        Credentials credentials = Credentials.create(platformPrivateKey);
        String platformWallet = credentials.getAddress();

        // 1. Host, Venue 존재 확인
        Host host = hostRepository.findById(hostId).orElseThrow(() -> new NotFoundException("호스트를 찾을 수 없습니다."));
        Venue venue = venueRepository.findById(request.venueId())
            .orElseThrow(() -> new NotFoundException("공연장을 찾을 수 없습니다."));

        // 2. ① shows 저장
        Show show = Show.builder().host(host).venue(venue).title(request.title()).posterUrl("temp")
            .showStartDate(request.showStartDate()).showEndDate(request.showEndDate())
            .reservationStartDate(request.reservationStartDate()).reservationEndDate(request.reservationEndDate())
            .playtime(request.playtime()).description(request.description()).purchaseLimit(request.purchaseLimit())
            .artist(request.artist()).platformFeeBps(800).platformWallet(platformWallet).status(ShowStatus.DRAFT)
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        show = showRepository.save(show);
        Long showId = show.getId();

        // 3. ② 포스터 S3 업로드 → posterUrl 업데이트
        String posterUrl = s3Uploader.upload(posterImage, showId);
        show.setPosterUrl(posterUrl);

        // 4. ③ sessions 저장 (회차)
        List<Session> sessions = request.sessionInfo().stream().map(s -> Session.builder().showId(showId)
            .sessionDate(s.sessionDate()).sessionStartTime(s.sessionStartTime()).build()).toList();
        sessions = sessionRepository.saveAll(sessions);

        // 5. ④ seat_grades 저장 (등급 × 구역)
        List<Long> allSectionIds = new ArrayList<>();

        for (AddShowRequest.GradeInfo grade : request.grade()) {
            for (Long sectionId : grade.sectionIds()) { // section마다 설정
                SeatGrade seatGrade = SeatGrade.builder().showId(showId).sectionId(sectionId)
                    .gradeName(grade.gradeName()).price(grade.price()).colorCode(grade.colorCode())
                    .ticketEffectId(grade.ticketEffectId()).build();
                seatGradeRepository.save(seatGrade);

                allSectionIds.add(sectionId);
            }
        }

        // 6. ⑤ refund_policies 저장 (환불 정책)
        for (AddShowRequest.RefundPolicyInfo policy : request.refundPolicy()) {
            RefundPolicy refundPolicy = RefundPolicy.builder().showId(showId).daysRemaining(policy.daysRemaining())
                .refundRate(policy.refundRate()).build();
            refundPolicyRepository.save(refundPolicy);
        }

        // 7. ⑥ stakeholders 저장 + StakeholderNFT 온체인 발행
        List<Stakeholder> savedStakeholders = new ArrayList<>();

        for (AddShowRequest.StakeholderInfo info : request.stakeholders()) {
            StakeholderRole role = StakeholderRole.valueOf(info.role().toUpperCase());
            Long userId = null;
            Long stakeholderHostId = null;

            if (role == StakeholderRole.ORGANIZER) {
                Host stakeholderHost = hostRepository.findByBusinessNoAndDeletedAtIsNull(info.businessNo())
                    .orElseThrow(() -> new NotFoundException("해당 사업자등록 번호로 가입된 주최측이 없습니다: " + info.businessNo()));
                stakeholderHostId = stakeholderHost.getId();
            } else {
                User user = userRepository.findByPhoneNumberAndDeletedAtIsNull(info.phoneNumber())
                    .orElseThrow(() -> new NotFoundException("이해 관계자를 찾을 수 없습니다: " + info.phoneNumber()));
                userId = user.getId();
            }

            Stakeholder stakeholder = Stakeholder.builder().showId(showId).userId(userId).hostId(stakeholderHostId)
                .role(role).shareBps(info.shareBps()).build();

            stakeholder = stakeholderRepository.save(stakeholder);
            savedStakeholders.add(stakeholder);
        }

        // 플랫폼도 Stakeholder로 추가 (800 bps = 8%)
        Stakeholder platformStakeholder = Stakeholder.builder().showId(showId).role(StakeholderRole.ORGANIZER)
            .shareBps(800).build();
        platformStakeholder = stakeholderRepository.save(platformStakeholder);
        savedStakeholders.add(platformStakeholder);

        // StakeholderNFT 온체인 발행 — 비동기 처리
        // Transaction PENDING 생성 후, 커밋 완료 시 afterCommit에서 비동기 민팅 시작
        List<Long> stakeholderIds = savedStakeholders.stream().map(Stakeholder::getId).toList();

        Transaction tx = Transaction.builder().type(Transaction.TransactionType.TRANSFER).amount(0L)
            .description("StakeholderNFT 발행 대기 중").txStatus(Transaction.TxStatus.PENDING).buyerId(hostId).build();
        tx = transactionRepository.save(tx);
        Long txId = tx.getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                blockchainAsyncWorker.processOnChainStakeholderMint(txId, stakeholderIds, platformWallet);
            }
        });

        // 8. ⑦ show_images 저장 (설명 이미지)
        if (descriptionImages != null && !descriptionImages.isEmpty()) {
            for (MultipartFile image : descriptionImages) {
                String imageUrl = s3Uploader.upload(image, showId);
                ShowImage showImage = ShowImage.of(show, imageUrl);
                showImageRepository.save(showImage);
            }
        }

        // 9. ⑧ session_seats 저장 (회차 × 좌석 = AVAILABLE)
        List<Seat> seats = seatRepository.findBySectionIdIn(allSectionIds);

        List<SessionSeat> sessionSeats = new ArrayList<>();

        for (Session session : sessions) {
            for (Seat seat : seats) {
                sessionSeats.add(SessionSeat.builder().sessionId(session.getId()).seatId(seat.getId())
                    .status(SeatStatus.AVAILABLE).build());
            }
        }
        sessionSeatRepository.saveAll(sessionSeats);

        return new CreateShowResponse(showId, txId);
    }

    @Override
    @Transactional
    public void deleteShow(Long hostId, Long showId) {
        // 1. 공연 존재 + 권한 확인
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));
        if (show.getStatus() != ShowStatus.DRAFT) {
            throw new BadRequestException("임시저장 상태의 공연만 삭제할 수 있습니다.");
        }
        if (!show.getHost().getId().equals(hostId)) {
            throw new ForbiddenException("본인이 등록한 공연만 삭제할 수 있습니다.");
        }

        // 2. FK 순서대로 삭제: session_seats → sessions → seat_grades → refund_policies →
        // stakeholders → show_images
        List<Long> sessionIds = sessionRepository.findByShowIdOrderBySessionDateAsc(showId).stream().map(Session::getId)
            .toList();
        if (!sessionIds.isEmpty()) {
            sessionSeatRepository.deleteBySessionIdIn(sessionIds);
        }
        sessionRepository.deleteAllByShowId(showId);
        seatGradeRepository.deleteAllByShowId(showId);
        refundPolicyRepository.deleteAllByShowId(showId);
        stakeholderRepository.deleteAllByShowId(showId);
        showImageRepository.deleteAllByShowId(showId);

        // 3. S3 이미지 삭제
        s3Uploader.deleteAllByShowId(showId);

        // 4. 공연 삭제
        showRepository.delete(show);
    }

    @Override
    @Transactional
    public void updateShow(Long hostId, Long showId, UpdateShowRequest request, MultipartFile posterImage,
        List<MultipartFile> descriptionImages) {

        // 1. 공연 존재 + 권한 확인
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));
        if (show.getStatus() != ShowStatus.DRAFT) {
            throw new BadRequestException("임시저장 상태의 공연만 수정할 수 있습니다.");
        }
        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 수정할 수 있습니다.");

        // ── 2. 기본 정보: null이 아닌 필드만 수정 ──
        if (request.title() != null)
            show.setTitle(request.title());
        if (request.venueId() != null) {
            Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new NotFoundException("공연장을 찾을 수 없습니다."));
            show.setVenue(venue);
        }
        if (request.artist() != null)
            show.setArtist(request.artist());
        if (request.description() != null)
            show.setDescription(request.description());
        if (request.playtime() != null)
            show.setPlaytime(request.playtime());
        if (request.purchaseLimit() != null)
            show.setPurchaseLimit(request.purchaseLimit());
        if (request.showStartDate() != null)
            show.setShowStartDate(request.showStartDate());
        if (request.showEndDate() != null)
            show.setShowEndDate(request.showEndDate());
        if (request.reservationStartDate() != null)
            show.setReservationStartDate(request.reservationStartDate());
        if (request.reservationEndDate() != null)
            show.setReservationEndDate(request.reservationEndDate());
        show.setUpdatedAt(LocalDateTime.now());

        // ── 3. 포스터 이미지: 파일이 있을 때만 교체 ──
        if (posterImage != null && !posterImage.isEmpty()) {
            String posterUrl = s3Uploader.upload(posterImage, showId);
            show.setPosterUrl(posterUrl);
        }

        // ── 4. 설명 이미지 ──
        List<String> existingUrls = request.existingDescriptionImageUrls();

        // existingUrls가 null이면 이미지 수정 안 함
        if (existingUrls != null) {
            // 1. 기존 이미지 중 유지 목록에 없는 것 -> DB 삭제 + s3삭제
            List<ShowImage> currentImages = showImageRepository.findAllByShow_Id(showId);
            // 유지 목록에 없는 것만 삭제
            for (ShowImage img : currentImages) {
                if (!existingUrls.contains(img.getImageUrl())) {
                    s3Uploader.delete(img.getImageUrl());
                    showImageRepository.delete(img);
                }
            }
        }

        // 새 이미지만 업로드 + 저장
        if (descriptionImages != null && !descriptionImages.isEmpty()) {
            for (MultipartFile image : descriptionImages) {
                String imageUrl = s3Uploader.upload(image, showId);
                showImageRepository.save(ShowImage.of(show, imageUrl));
            }
        }

        // ── 5. 회차 (sessionInfo): null이면 건드리지 않음 ──
        if (request.sessionInfo() != null) {
            // 기존 session_seats → sessions 삭제 (FK 순서)
            List<Long> oldSessionIds = sessionRepository.findByShowIdOrderBySessionDateAsc(showId).stream()
                .map(Session::getId).toList();
            if (!oldSessionIds.isEmpty()) {
                sessionSeatRepository.deleteBySessionIdIn(oldSessionIds);
            }
            sessionRepository.deleteAllByShowId(showId);

            // 새 회차 저장
            List<Session> newSessions = sessionRepository
                .saveAll(request.sessionInfo().stream().map(s -> Session.builder().showId(showId)
                    .sessionDate(s.sessionDate()).sessionStartTime(s.sessionStartTime()).build()).toList());

            // 새 session_seats 재생성 (등급 정보가 같이 왔으면 새 등급 기준, 아니면 기존 등급 기준)
            List<Long> sectionIds;
            if (request.grade() != null) {
                sectionIds = request.grade().stream().flatMap(g -> g.sectionIds().stream()).distinct().toList();
            } else {
                sectionIds = seatGradeRepository.findByShowId(showId).stream().map(SeatGrade::getSectionId).distinct()
                    .toList();
            }

            if (!sectionIds.isEmpty()) {
                List<Seat> seats = seatRepository.findBySectionIdIn(sectionIds);
                List<SessionSeat> sessionSeats = new ArrayList<>();
                for (Session session : newSessions) {
                    for (Seat seat : seats) {
                        sessionSeats.add(SessionSeat.builder().sessionId(session.getId()).seatId(seat.getId())
                            .status(SeatStatus.AVAILABLE).build());
                    }
                }
                sessionSeatRepository.saveAll(sessionSeats);
            }
        }

        // ── 6. 등급 (grade): null이면 건드리지 않음 ──
        if (request.grade() != null) {
            seatGradeRepository.deleteAllByShowId(showId);

            for (UpdateShowRequest.GradeInfo grade : request.grade()) {
                for (Long sectionId : grade.sectionIds()) {
                    seatGradeRepository.save(SeatGrade.builder().showId(showId).sectionId(sectionId)
                        .gradeName(grade.gradeName()).price(grade.price()).colorCode(grade.colorCode())
                        .ticketEffectId(grade.ticketEffectId()).build());
                }
            }

            // 등급만 바뀌고 회차는 안 바뀐 경우 → session_seats 재생성
            if (request.sessionInfo() == null) {
                List<Long> existingSessionIds = sessionRepository.findByShowIdOrderBySessionDateAsc(showId).stream()
                    .map(Session::getId).toList();
                if (!existingSessionIds.isEmpty()) {
                    sessionSeatRepository.deleteBySessionIdIn(existingSessionIds);

                    List<Long> newSectionIds = request.grade().stream().flatMap(g -> g.sectionIds().stream()).distinct()
                        .toList();
                    List<Seat> seats = seatRepository.findBySectionIdIn(newSectionIds);
                    List<Session> existingSessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);

                    List<SessionSeat> sessionSeats = new ArrayList<>();
                    for (Session session : existingSessions) {
                        for (Seat seat : seats) {
                            sessionSeats.add(SessionSeat.builder().sessionId(session.getId()).seatId(seat.getId())
                                .status(SeatStatus.AVAILABLE).build());
                        }
                    }
                    sessionSeatRepository.saveAll(sessionSeats);
                }
            }
        }

        // ── 7. 환불 정책 (refundPolicy): null이면 건드리지 않음 ──
        if (request.refundPolicy() != null) {
            refundPolicyRepository.deleteAllByShowId(showId);
            for (UpdateShowRequest.RefundPolicyInfo policy : request.refundPolicy()) {
                refundPolicyRepository.save(RefundPolicy.builder().showId(showId).daysRemaining(policy.daysRemaining())
                    .refundRate(policy.refundRate()).build());
            }
        }
    }

    private ShowItem toShowItem(Show s) {
        return new ShowItem(s.getId(), s.getTitle(), s.getPosterUrl(), s.getVenue().getName(), s.getPurchaseLimit(),
            s.getVenue().getRegion().getName(),
            new ShowItem.ShowPeriod(s.getShowStartDate().toLocalDate(), s.getShowEndDate().toLocalDate()),
            new ShowItem.ReservationPeriod(s.getReservationStartDate(), s.getReservationEndDate()),
            s.getStatus().name());
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

}
