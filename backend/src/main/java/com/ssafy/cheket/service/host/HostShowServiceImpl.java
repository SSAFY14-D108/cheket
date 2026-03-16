package com.ssafy.cheket.service.host;

import com.ssafy.cheket.config.s3.S3Uploader;
import com.ssafy.cheket.dto.host.response.GetHostShowDetailResponse;
import com.ssafy.cheket.dto.show.request.AddShowRequest;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.ShowItem;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.entity.ticket.TicketEffect;
import com.ssafy.cheket.entity.user.User;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Credentials;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final S3Uploader s3Uploader;
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
            show.getDescription(), show.getArtist(), show.getPurchaseLimit(), likeRepository.countByShowId(showId),

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
            show.getStatus(), show.getCreatedAt(), show.getUpdatedAt());
    }

    @Override
    @Transactional
    public Long createShow(Long hostId, AddShowRequest request, MultipartFile posterImage,
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
        List<Session> sessions = request.sessions().stream().map(s -> Session.builder().showId(showId)
            .sessionDate(s.sessionDate()).sessionStartTime(s.sessionStartTime()).build()).toList();
        sessions = sessionRepository.saveAll(sessions);

        // 5. ④ seat_grades 저장 (등급 × 구역)
        List<Long> allSectionIds = new ArrayList<>();

        for (AddShowRequest.GradeInfo grade : request.grades()) {
            for (Long sectionId : grade.sectionIds()) { // section마다 설정
                SeatGrade seatGrade = SeatGrade.builder().showId(showId).sectionId(sectionId)
                    .gradeName(grade.gradeName()).price(grade.price()).colorCode(grade.colorCode())
                    .ticketEffectId(grade.ticketEffectId()).build();
                seatGradeRepository.save(seatGrade);

                allSectionIds.add(sectionId);
            }
        }

        // 6. ⑤ refund_policies 저장 (환불 정책)
        for (AddShowRequest.RefundPolicyInfo policy : request.refundPolicies()) {
            RefundPolicy refundPolicy = RefundPolicy.builder().showId(showId).daysRemaining(policy.daysRemaining())
                .refundRate(policy.refundRate()).build();
            refundPolicyRepository.save(refundPolicy);
        }

        // 7. ⑥ stakeholders 저장 (수익 배분)
        for (AddShowRequest.StakeholderInfo info : request.stakeholders()) {
            StakeholderRole role = StakeholderRole.valueOf(info.role().toUpperCase());
            Long userId = null;
            Long stakeholderHostId = null;

            if (role == StakeholderRole.ORGANIZER) {
                // ORGANIZER = 주최측 -> 사업자 등록번호로 조회
                Host stakeholderHost = hostRepository.findByBusinessNoAndDeletedAtIsNull(info.businessNo())
                    .orElseThrow(() -> new NotFoundException("해당 사업자등록 번호로 가입된 주최측이 없습니다: " + info.businessNo()));
                stakeholderHostId = stakeholderHost.getId();
            } else {
                // ARTIST = 일반 사용자 -> phoneNumber로 조회
                User user = userRepository.findByPhoneNumberAndDeletedAtIsNull(info.phoneNumber())
                    .orElseThrow(() -> new NotFoundException("이해 관계자를 찾을 수 없습니다: " + info.phoneNumber()));
                userId = user.getId();
            }

            Stakeholder stakeholder = Stakeholder.builder().showId(showId).userId(userId).hostId(stakeholderHostId)
                .role(role).shareBps(info.shareBps()).build();

            stakeholderRepository.save(stakeholder);
        }

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
                    .status(SessionSeat.SeatStatus.AVAILABLE).build());
            }
        }
        sessionSeatRepository.saveAll(sessionSeats);

        return showId;
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
