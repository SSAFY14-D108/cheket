package com.ssafy.cheket.service.show;

import com.ssafy.cheket.config.s3.S3Uploader;
import com.ssafy.cheket.dto.show.request.PurchaseSessionSeatRequest;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.enums.SeatStatus;
import com.ssafy.cheket.dto.show.request.SaveSearchKeywordRequest;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.ShowSort;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.*;
import com.ssafy.cheket.repository.show.projection.HeldSeatLockProjection;
import com.ssafy.cheket.repository.show.projection.PurchaseSessionSeatProjection;
import com.ssafy.cheket.repository.show.projection.SessionListProjection;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {

    private static final Duration SEAT_LOCK_TTL = Duration.ofMinutes(5L);
    private static final String SEAT_LOCK_PREFIX = "seat-lock";
    private static final long HELD_CLEANUP_DELAY_MS = 30_000L;

    private final ShowRepository showRepository;
    private final LikeRepository likeRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final SectionRepository sectionRepository;
    private final SessionRepository sessionRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;
    private final ShowImageRepository showImageRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final S3Uploader s3Uploader;
    private final StringRedisTemplate redisTemplate;

    // 공연 검색 및 목록 조회
    @Override
    public GetShowListResponse<ShowItem> getShowList(List<Integer> regions, ShowSort sort, String keyword, int page,
        int size) {
        List<Integer> normalizedRegions = (regions == null) ? List.of() : regions;
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        ShowSort normalizedSort = (sort == null) ? ShowSort.POPULAR : sort;
        Pageable pageable;
        Page<Show> result;
        LocalDateTime now = LocalDateTime.now();

        switch (normalizedSort) {
            case POPULAR -> {
                pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
                result = showRepository.searchOrderByPopular(normalizedRegions, (long) normalizedRegions.size(),
                    normalized, pageable);
            }
            case LATEST -> {
                pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
                result = showRepository.search(normalizedRegions, (long) normalizedRegions.size(), normalized,
                    pageable);
            }
            case OPEN_SOON -> {
                pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100),
                    Sort.by(Sort.Direction.ASC, "reservationStartDate"));
                result = showRepository.searchOrderByOpenSoon(normalizedRegions, (long) normalizedRegions.size(),
                    normalized, now, pageable);
            }
            case DEADLINE -> {
                pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100),
                    Sort.by(Sort.Direction.ASC, "reservationEndDate"));
                result = showRepository.searchOrderByDeadline(normalizedRegions, (long) normalizedRegions.size(),
                    normalized, now, pageable);
            }
            default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
        }

        List<ShowItem> items = result.getContent().stream().map(this::toShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    // 공연 상세 조회
    @Override
    public GetShowDetailResponse getShowDetail(Long showId, Long userId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        int likeCount = likeRepository.countByShowId(showId);

        boolean isLiked = false;
        if (userId != null)
            isLiked = likeRepository.existsByUserIdAndShowId(userId, showId);

        List<GetShowDetailResponse.GradeInfo> grades = seatGradeRepository.findByShowId(showId).stream()
            .sorted(Comparator.comparing(SeatGrade::getSectionId))
            .map(seatGrade -> new GetShowDetailResponse.GradeInfo(seatGrade.getSectionId(), seatGrade.getGradeName(),
                seatGrade.getPrice(), seatGrade.getColorCode()))
            .toList();

        List<GetShowDetailResponse.RefundPolicyInfo> refundPolicies = refundPolicyRepository
            .findByShowIdOrderByDaysRemainingDesc(showId).stream()
            .map(refundPolicy -> new GetShowDetailResponse.RefundPolicyInfo(refundPolicy.getDaysRemaining(),
                refundPolicy.getRefundRate()))
            .toList();

        List<String> descriptionImages = showImageRepository.findAllByShow_Id(showId).stream()
            .map(ShowImage::getImageUrl).toList();

        return new GetShowDetailResponse(show.getId(), show.getTitle(), show.getPosterUrl(), show.getVenue().getName(),
            show.getPurchaseLimit(), show.getVenue().getRegion().getName(),
            new GetShowDetailResponse.ShowPeriod(show.getShowStartDate().toLocalDate(),
                show.getShowEndDate().toLocalDate()),
            new GetShowDetailResponse.ReservationPeriod(show.getReservationStartDate(), show.getReservationEndDate()),
            show.getStatus(), show.getDescription(), show.getArtist(), show.getPlaytime(), isLiked, likeCount, grades,
            refundPolicies, descriptionImages);
    }

    // 회차 목록 조회
    @Override
    public List<SessionListResponse> getSessionList(Long showId) {
        if (!showRepository.existsById(showId))
            throw new NotFoundException("존재하지 않는 공연입니다.");

        List<SessionListProjection> results = sessionRepository.findSessionListByShowId(showId);

        return results.stream()
            .map(result -> new SessionListResponse(result.getSessionId(), result.getSessionDate().toLocalDate(),
                result.getSessionStartTime().toLocalTime(), result.getRemainingSeats(), result.getTotalSeats()))
            .toList();
    }

    // 좌석 배치도 조회
    @Override
    public List<GetSeatsResponse> getSeats(Long showId, Long sessionId) {
        if (!sessionRepository.existsByIdAndShowId(sessionId, showId))
            throw new NotFoundException("해당 공연의 회차를 찾을 수 없습니다.");

        List<SeatRowDto> rows = sessionSeatRepository.findSeatRowsByShowIdAndSessionId(showId, sessionId);

        // 구역별로 묶기
        Map<Long, SectionGroup> grouped = new LinkedHashMap<>();
        for (SeatRowDto row : rows) {
            grouped
                // 구역이 없으면 새로 생성
                .computeIfAbsent(row.sectionId(),
                    key -> new SectionGroup(row.sectionId(), row.sectionName(), row.gradeName(), row.price(),
                        row.colorCode(), new ArrayList<>()))
                // 해당 구역에 좌석 추가
                .seats().add(new SeatItemResponse(row.sessionSeatId(), row.seatId(), row.rowNum(), row.colNum(),
                    row.seatNo(), row.status().name()));
        }

        return grouped.values().stream().map(group -> new GetSeatsResponse(group.sectionId(), group.sectionName(),
            group.gradeName(), group.price(), group.colorCode(), group.seats())).toList();
    }

    // 공연장 목록 조회
    @Override
    public List<GetVenuesResponse> getVenues() {
        List<Venue> venues = venueRepository.findAll();
        return venues.stream().map(venue -> {
            List<Section> sections = sectionRepository.findByVenueId(venue.getId());
            List<Long> sectionIds = sections.stream().map(Section::getId).toList();
            int capacity = sectionIds.isEmpty() ? 0 : seatRepository.countBySectionIdIn(sectionIds);
            return new GetVenuesResponse(venue.getId(), venue.getName(), capacity);
        }).toList();
    }

    // 공연별 환불 정책 조회
    @Override
    public GetRefundResponse getRefund(Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));
        List<RefundPolicy> refundPolicies = refundPolicyRepository.findByShowIdOrderByDaysRemainingDesc(showId);
        List<GetRefundResponse.RefundPolicyInfo> refundPolicyInfoList = refundPolicies.stream()
            .map(policy -> new GetRefundResponse.RefundPolicyInfo(policy.getDaysRemaining(), policy.getRefundRate()))
            .toList();
        return new GetRefundResponse(refundPolicyInfoList, show.getShowStartDate().toLocalDate());
    }

    // 오픈 예정 공연 5개 조회
    @Override
    public GetUpcomingResponse getUpcoming() {
        Pageable pageable = PageRequest.of(0, 5);
        List<ShowItem> items = showRepository.findUpcomingTop5ByLikeCount(pageable).stream().map(this::toShowItem)
            .toList();

        return new GetUpcomingResponse(items);
    }

    @Override
    public List<GetSectionsResponse> getSections(Long venueId) {
        List<Section> sections = sectionRepository.findByVenueId(venueId);

        return sections.stream().map(section -> new GetSectionsResponse(section.getId(), section.getSectionName()))
            .toList();
    }

    // 공연 이미지 조회
    @Override
    public List<ShowImage> getShowImages(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new NotFoundException("해당 공연은 존재하지 않습니다.");
        }

        return showImageRepository.findAllByShow_Id(showId);
    }

    // 공연 이미지 삭제
    @Override
    public void deleteShowImages(Long showId) {
        s3Uploader.deleteAllByShowId(showId);
        showImageRepository.deleteAllByShowId(showId);
    }

    // 좌석 선점(결제하기 버튼)
    @Override
    @Transactional
    public PurchaseSessionSeatResponse purchaseSessionSeats(Long userId, Long showId, Long sessionId,
        PurchaseSessionSeatRequest request) {
        if (!sessionRepository.existsByIdAndShowId(sessionId, showId)) {
            throw new NotFoundException("존재하지 않는 공연 또는 회차입니다.");
        }

        if (request == null || request.sessionSeatIds() == null || request.sessionSeatIds().isEmpty()) {
            throw new BadRequestException("좌석 선택은 필수입니다.");
        }

        List<Long> requestedSeatIds = request.sessionSeatIds();
        List<Long> distinctSeatIds = requestedSeatIds.stream().distinct().toList();
        if (requestedSeatIds.size() != distinctSeatIds.size()) {
            throw new BadRequestException("중복된 자리 선택입니다.");
        }

        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));
        long purchasedCount = ticketRepository.countByUserIdAndSessionId(userId, sessionId);
        if (purchasedCount + distinctSeatIds.size() > show.getPurchaseLimit()) {
            throw new ConflictException(show.getPurchaseLimit() + "매까지만 선택 가능합니다.");
        }

        List<PurchaseSessionSeatProjection> rows = showRepository.findPurchaseSessionSeats(showId, sessionId,
            distinctSeatIds);
        if (rows.size() != distinctSeatIds.size()) {
            throw new NotFoundException("존재하지 않는 좌석입니다.");
        }

        List<SessionSeat> seats = sessionSeatRepository.findAllById(distinctSeatIds);
        if (seats.size() != distinctSeatIds.size()) {
            throw new NotFoundException("존재하지 않는 좌석입니다.");
        }

        Map<Long, SessionSeat> seatBySeatId = seats.stream()
            .collect(Collectors.toMap(SessionSeat::getId, seat -> seat));

        // Redis 락이 만료된 HELD 좌석은 다시 AVAILABLE 로 상태 변경
        List<SessionSeat> staleHeldSeats = new ArrayList<>();
        for (SessionSeat seat : seats) {
            if (seat.getStatus() != SeatStatus.HELD) {
                continue;
            }
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(seatLockKey(showId, sessionId, seat.getId())))) {
                seat.setStatus(SeatStatus.AVAILABLE);
                staleHeldSeats.add(seat);
            }
        }
        if (!staleHeldSeats.isEmpty()) {
            sessionSeatRepository.saveAll(staleHeldSeats);
        }

        Set<Long> unavailableSeatIds = seats.stream().filter(seat -> seat.getStatus() != SeatStatus.AVAILABLE)
            .map(SessionSeat::getId).collect(Collectors.toSet());

        Map<Long, PurchaseSessionSeatProjection> rowBySeatId = new HashMap<>();
        for (PurchaseSessionSeatProjection row : rows) {
            rowBySeatId.put(row.getSessionSeatId(), row);
        }

        List<PurchaseSessionSeatResponse.SessionSeatInfo> failure = new ArrayList<>();
        List<Long> acquiredSeatIds = new ArrayList<>();
        List<String> acquiredLockKeys = new ArrayList<>();
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        for (Long seatId : distinctSeatIds) {
            PurchaseSessionSeatProjection row = rowBySeatId.get(seatId);
            PurchaseSessionSeatResponse.SessionSeatInfo info = toSeatInfo(row);

            if (unavailableSeatIds.contains(seatId)) {
                failure.add(info);
                continue;
            }

            String lockKey = seatLockKey(showId, sessionId, seatId);
            Boolean locked = ops.setIfAbsent(lockKey, "1", SEAT_LOCK_TTL);
            if (Boolean.TRUE.equals(locked)) {
                acquiredSeatIds.add(seatId);
                acquiredLockKeys.add(lockKey);
            } else {
                failure.add(info);
            }
        }

        PurchaseSessionSeatProjection first = rows.get(0);
        int totalPrice = first.getTotalPrice() == null ? 0 : first.getTotalPrice();

        if (!failure.isEmpty() || acquiredSeatIds.size() != distinctSeatIds.size()) {
            acquiredLockKeys.forEach(redisTemplate::delete);
            return new PurchaseSessionSeatResponse(LocalDateTime.now().plus(SEAT_LOCK_TTL), first.getShowTitle(),
                first.getSessionDate(), first.getVenueName(), new PurchaseSessionSeatResponse.Seats(List.of(), failure),
                totalPrice);
        }

        List<SessionSeat> heldSeats = new ArrayList<>(distinctSeatIds.size());
        for (Long seatId : distinctSeatIds) {
            SessionSeat seat = seatBySeatId.get(seatId);
            seat.setStatus(SeatStatus.HELD);
            heldSeats.add(seat);
        }
        try {
            sessionSeatRepository.saveAll(heldSeats);
        } catch (RuntimeException e) {
            acquiredLockKeys.forEach(redisTemplate::delete);
            throw e;
        }

        List<PurchaseSessionSeatResponse.SessionSeatInfo> success = distinctSeatIds.stream().map(rowBySeatId::get)
            .map(this::toSeatInfo).toList();
        return new PurchaseSessionSeatResponse(LocalDateTime.now().plus(SEAT_LOCK_TTL), first.getShowTitle(),
            first.getSessionDate(), first.getVenueName(), new PurchaseSessionSeatResponse.Seats(success, failure),
            totalPrice);
    }

    private PurchaseSessionSeatResponse.SessionSeatInfo toSeatInfo(PurchaseSessionSeatProjection row) {
        return new PurchaseSessionSeatResponse.SessionSeatInfo(row.getSessionSeatId(), row.getSectionName(),
            row.getSeatNo(), row.getGrade(), row.getPrice());
    }

    private String seatLockKey(Long showId, Long sessionId, Long sessionSeatId) {
        return "%s:%d:%d:%d".formatted(SEAT_LOCK_PREFIX, showId, sessionId, sessionSeatId);
    }

    // Held 로 변경된 좌석이 TTL(5분) 이내에 구매되지 않으면 AVAILABLE 로 변경하도록 스케줄링
    @Scheduled(fixedDelay = HELD_CLEANUP_DELAY_MS)
    @Transactional
    public void releaseExpiredHeldSeats() {
        List<HeldSeatLockProjection> heldSeats = sessionSeatRepository.findHeldSeatLockTargets();
        if (heldSeats.isEmpty()) {
            return;
        }

        List<Long> expiredSeatIds = new ArrayList<>();
        for (HeldSeatLockProjection heldSeat : heldSeats) {
            String lockKey = seatLockKey(heldSeat.getShowId(), heldSeat.getSessionId(), heldSeat.getSessionSeatId());
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
                expiredSeatIds.add(heldSeat.getSessionSeatId());
            }
        }

        if (expiredSeatIds.isEmpty()) {
            return;
        }

        List<SessionSeat> seatsToRelease = sessionSeatRepository.findAllById(expiredSeatIds);
        List<SessionSeat> changedSeats = new ArrayList<>();
        for (SessionSeat seat : seatsToRelease) {
            if (seat.getStatus() == SeatStatus.HELD) {
                seat.setStatus(SeatStatus.AVAILABLE);
                changedSeats.add(seat);
            }
        }
        if (!changedSeats.isEmpty()) {
            sessionSeatRepository.saveAll(changedSeats);
        }
    }

    // 공연 검색 기록 저장
    @Transactional
    @Override
    public void saveSearchKeyword(SaveSearchKeywordRequest request, Long userId) {
        String normalizedKeyword = normalizeKeyword(request.keyword());

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        SearchHistory searchHistory = SearchHistory.create(user, normalizedKeyword);
        searchHistoryRepository.save(searchHistory);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            throw new BadRequestException("검색 키워드는 비어 있을 수 없습니다.");
        }

        String normalized = keyword.trim();

        if (normalized.isEmpty()) {
            throw new BadRequestException("검색 키워드는 비어 있을 수 없습니다.");
        }

        if (normalized.length() > 100) {
            throw new BadRequestException("검색 키워드는 100자 이하여야 합니다.");
        }

        return normalized;
    }

    private ShowItem toShowItem(Show s) {
        return new ShowItem(s.getId(), s.getTitle(), s.getPosterUrl(), s.getVenue().getName(), s.getPurchaseLimit(),
            s.getVenue().getRegion().getName(),
            new ShowItem.ShowPeriod(s.getShowStartDate().toLocalDate(), s.getShowEndDate().toLocalDate()),
            new ShowItem.ReservationPeriod(s.getReservationStartDate(), s.getReservationEndDate()),
            s.getStatus().name());
    }

    private record SectionGroup(Long sectionId, String sectionName, String gradeName, Integer price, String colorCode,
        List<SeatItemResponse> seats) {
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
