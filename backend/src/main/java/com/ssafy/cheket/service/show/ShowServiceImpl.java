package com.ssafy.cheket.service.show;

import com.ssafy.cheket.client.ai.RecommendationAiClient;
import com.ssafy.cheket.client.ai.dto.ArtistPreferencePayload;
import com.ssafy.cheket.client.ai.dto.CandidateShowPayload;
import com.ssafy.cheket.client.ai.dto.RecommendationItemPayload;
import com.ssafy.cheket.client.ai.dto.RecommendationRequestPayload;
import com.ssafy.cheket.client.ai.dto.RecommendationResponsePayload;
import com.ssafy.cheket.config.s3.S3Uploader;
import com.ssafy.cheket.dto.show.request.PurchaseSessionSeatRequest;
import com.ssafy.cheket.dto.show.request.SaveSearchKeywordRequest;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.SeatStatus;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {

    private static final Duration SEAT_LOCK_TTL = Duration.ofMinutes(5L);
    private static final String SEAT_LOCK_PREFIX = "seat-lock";
    private static final long HELD_CLEANUP_DELAY_MS = 30_000L;
    private static final int RECENT_KEYWORD_LIMIT = 5;
    private static final int USER_SIGNAL_SHOW_LIMIT = 10;
    private static final int CANDIDATE_FETCH_LIMIT = 100;
    private static final int DEFAULT_RECOMMENDATION_SIZE = 10;
    private static final boolean DEFAULT_EXCLUDE_LIKED = false;

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
    private final RecommendationEmbeddingStore recommendationEmbeddingStore;
    private final RecommendationAiClient recommendationAiClient;
    private final S3Uploader s3Uploader;
    private final StringRedisTemplate redisTemplate;

    // 공연 검색 및 목록 조회
    @Override
    public GetShowListResponse<ShowItem> getShowList(List<Integer> regions, ShowSort sort, String keyword,
        boolean includeEnded, int page, int size) {

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
                    normalized, includeEnded, now, pageable);
            }
            case LATEST -> {
                pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100),
                    Sort.by(Sort.Direction.DESC, "createdAt"));
                result = showRepository.search(normalizedRegions, (long) normalizedRegions.size(), normalized,
                    includeEnded, now, pageable);
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
    public GetRecommendationsResponse getRecommendations(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Show> likedShows = limitSignals(likeRepository.findLikedShowsByUserId(userId));
        List<Show> purchasedShows = limitSignals(ticketRepository.findPurchasedShowsByUserId(userId));
        List<String> recentKeywords = getRecentKeywords(userId);

        int candidateLimit = clamp(DEFAULT_RECOMMENDATION_SIZE * 10, DEFAULT_RECOMMENDATION_SIZE,
            CANDIDATE_FETCH_LIMIT);
        List<Show> candidates = showRepository.findRecommendationCandidates(userId, DEFAULT_EXCLUDE_LIKED, now).stream()
            .limit(candidateLimit).toList();

        if (candidates.isEmpty()) {
            return new GetRecommendationsResponse(List.of());
        }

        RecommendationRequestPayload payload = buildRecommendationRequest(userId, likedShows, purchasedShows,
            recentKeywords, candidates);

        List<RecommendedShowItem> recommendations;
        try {
            RecommendationResponsePayload response = recommendationAiClient.recommend(payload);
            recommendations = mergeAiRecommendations(response.recommendations(), candidates,
                DEFAULT_RECOMMENDATION_SIZE);
        } catch (RuntimeException e) {
            recommendations = buildLocalFallbackRecommendations(likedShows, purchasedShows, recentKeywords, candidates,
                DEFAULT_RECOMMENDATION_SIZE);
        }

        return new GetRecommendationsResponse(recommendations);
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

    private RecommendationRequestPayload buildRecommendationRequest(Long userId, List<Show> likedShows,
        List<Show> purchasedShows, List<String> recentKeywords, List<Show> candidates) {
        List<RecommendationEmbeddingStore.WeightedShowSignal> embeddingSignals = new ArrayList<>();
        for (Show show : purchasedShows) {
            embeddingSignals.add(new RecommendationEmbeddingStore.WeightedShowSignal(show.getId(), 2.0));
        }
        for (Show show : likedShows) {
            embeddingSignals.add(new RecommendationEmbeddingStore.WeightedShowSignal(show.getId(), 1.0));
        }

        Map<Long, List<Double>> candidateEmbeddings = recommendationEmbeddingStore
            .findEmbeddingsByShowIds(candidates.stream().map(Show::getId).toList());

        return new RecommendationRequestPayload(userId,
            recommendationEmbeddingStore.buildWeightedUserEmbedding(embeddingSignals),
            buildUserProfileText(likedShows, purchasedShows, recentKeywords),
            buildArtistPreferences(likedShows, purchasedShows), recentKeywords,
            candidates.stream().map(show -> toCandidatePayload(show, candidateEmbeddings)).toList());
    }

    private List<Show> limitSignals(List<Show> shows) {
        return shows.stream().limit(USER_SIGNAL_SHOW_LIMIT).toList();
    }

    private List<String> getRecentKeywords(Long userId) {
        LinkedHashSet<String> deduplicated = new LinkedHashSet<>();
        for (SearchHistory searchHistory : searchHistoryRepository.findTop10ByUser_IdOrderByCreatedAtDesc(userId)) {
            String keyword = searchHistory.getKeyword();
            if (keyword == null || keyword.isBlank()) {
                continue;
            }
            deduplicated.add(keyword.trim());
            if (deduplicated.size() >= RECENT_KEYWORD_LIMIT) {
                break;
            }
        }
        return List.copyOf(deduplicated);
    }

    private List<ArtistPreferencePayload> buildArtistPreferences(List<Show> likedShows, List<Show> purchasedShows) {
        Map<String, Double> artistWeights = new LinkedHashMap<>();

        for (Show show : purchasedShows) {
            addArtistPreferenceWeight(artistWeights, show.getArtist(), 2.0);
        }
        for (Show show : likedShows) {
            addArtistPreferenceWeight(artistWeights, show.getArtist(), 1.0);
        }

        return artistWeights.entrySet().stream()
            .map(entry -> new ArtistPreferencePayload(entry.getKey(), entry.getValue())).toList();
    }

    private void addArtistPreferenceWeight(Map<String, Double> artistWeights, String artist, double delta) {
        if (artist == null || artist.isBlank()) {
            return;
        }
        artistWeights.merge(artist.trim(), delta, Double::sum);
    }

    private String buildUserProfileText(List<Show> likedShows, List<Show> purchasedShows, List<String> recentKeywords) {
        List<String> parts = new ArrayList<>();

        for (Show show : purchasedShows) {
            String text = buildEmbeddingText(show);
            parts.add(text);
            parts.add(text);
        }
        for (Show show : likedShows) {
            parts.add(buildEmbeddingText(show));
        }
        if (!recentKeywords.isEmpty()) {
            parts.add("최근 검색어: " + String.join(", ", recentKeywords));
        }

        return String.join(" || ", parts);
    }

    private CandidateShowPayload toCandidatePayload(Show show, Map<Long, List<Double>> candidateEmbeddings) {
        return new CandidateShowPayload(show.getId(), show.getArtist(), show.getTitle(), show.getVenue().getName(),
            candidateEmbeddings.getOrDefault(show.getId(), List.of()), buildEmbeddingText(show),
            computeTicketingState(show), computeShowState(show), show.getShowStartDate().toLocalDate().toString());
    }

    private String buildEmbeddingText(Show show) {
        List<String> parts = new ArrayList<>();
        parts.add("제목: " + show.getTitle());
        if (show.getArtist() != null && !show.getArtist().isBlank()) {
            parts.add("아티스트: " + show.getArtist());
        }
        parts.add("설명: " + show.getDescription());
        parts.add("공연장: " + show.getVenue().getName());
        parts.add("지역: " + show.getVenue().getRegion().getName());
        if (show.getHost() != null && show.getHost().getCompanyName() != null
            && !show.getHost().getCompanyName().isBlank()) {
            parts.add("주최사: " + show.getHost().getCompanyName());
        }
        return String.join(" / ", parts);
    }

    private List<RecommendedShowItem> mergeAiRecommendations(List<RecommendationItemPayload> aiItems,
        List<Show> candidates, int size) {
        Map<Long, Show> showById = candidates.stream().collect(Collectors.toMap(Show::getId, show -> show));
        return aiItems.stream().map(item -> {
            Show show = showById.get(item.showId());
            if (show == null) {
                return null;
            }
            return toRecommendedShowItem(show, item.score());
        }).filter(item -> item != null).limit(size).toList();
    }

    private List<RecommendedShowItem> buildLocalFallbackRecommendations(List<Show> likedShows,
        List<Show> purchasedShows, List<String> recentKeywords, List<Show> candidates, int size) {
        Map<String, Double> tokenWeights = buildInterestTokenWeights(likedShows, purchasedShows);
        Map<String, Double> artistWeights = buildArtistWeightMap(likedShows, purchasedShows);

        return candidates.stream()
            .map(show -> computeLocalRecommendation(show, tokenWeights, artistWeights, recentKeywords))
            .sorted(Comparator.comparing(LocalRecommendation::score).reversed()).limit(size)
            .map(result -> toRecommendedShowItem(result.show(), result.score())).toList();
    }

    private Map<String, Double> buildInterestTokenWeights(List<Show> likedShows, List<Show> purchasedShows) {
        Map<String, Double> weights = new HashMap<>();

        for (Show show : purchasedShows) {
            addWeightedTokens(weights, show, 2.0);
        }
        for (Show show : likedShows) {
            addWeightedTokens(weights, show, 1.0);
        }

        return weights;
    }

    private Map<String, Double> buildArtistWeightMap(List<Show> likedShows, List<Show> purchasedShows) {
        Map<String, Double> artistWeights = new HashMap<>();
        for (Show show : purchasedShows) {
            mergeNormalizedWeight(artistWeights, show.getArtist(), 2.0);
        }
        for (Show show : likedShows) {
            mergeNormalizedWeight(artistWeights, show.getArtist(), 1.0);
        }
        return artistWeights;
    }

    private void addWeightedTokens(Map<String, Double> weights, Show show, double multiplier) {
        for (String token : extractMeaningfulTokens(buildEmbeddingText(show))) {
            weights.merge(token, multiplier, Double::sum);
        }
    }

    private void mergeNormalizedWeight(Map<String, Double> weights, String rawValue, double delta) {
        String normalized = normalizeText(rawValue);
        if (!normalized.isBlank()) {
            weights.merge(normalized, delta, Double::sum);
        }
    }

    private LocalRecommendation computeLocalRecommendation(Show show, Map<String, Double> tokenWeights,
        Map<String, Double> artistWeights, List<String> recentKeywords) {
        double textScore = computeTextSimilarity(tokenWeights, show);
        double artistScore = computeArtistMatchScore(artistWeights, show.getArtist());
        double searchScore = computeKeywordMatchScore(recentKeywords, show);
        double freshnessScore = computeFreshnessScore(computeTicketingState(show), computeShowState(show));
        double timingScore = computeTimingScore(show);

        double finalScore = Math.min(textScore + artistScore + searchScore + freshnessScore + timingScore, 1.0);
        String reason = buildLocalReason(show, textScore, artistScore, searchScore, freshnessScore, timingScore,
            tokenWeights.isEmpty() && artistWeights.isEmpty() && recentKeywords.isEmpty());
        return new LocalRecommendation(show, roundScore(finalScore), reason);
    }

    private double computeTextSimilarity(Map<String, Double> tokenWeights, Show show) {
        if (tokenWeights.isEmpty()) {
            return 0.0;
        }

        Set<String> candidateTokens = extractMeaningfulTokens(buildEmbeddingText(show));
        double matchedWeight = candidateTokens.stream().mapToDouble(token -> tokenWeights.getOrDefault(token, 0.0))
            .sum();
        return Math.min(matchedWeight / 12.0, 0.42);
    }

    private double computeArtistMatchScore(Map<String, Double> artistWeights, String artist) {
        String normalizedArtist = normalizeText(artist);
        if (normalizedArtist.isBlank()) {
            return 0.0;
        }
        double weight = artistWeights.getOrDefault(normalizedArtist, 0.0);
        return Math.min(weight / 2.0, 1.0) * 0.38;
    }

    private double computeKeywordMatchScore(List<String> recentKeywords, Show show) {
        if (recentKeywords.isEmpty()) {
            return 0.0;
        }

        String candidateText = normalizeText(buildEmbeddingText(show));
        for (String keyword : recentKeywords) {
            String normalizedKeyword = normalizeText(keyword);
            if (!normalizedKeyword.isBlank() && candidateText.contains(normalizedKeyword)) {
                return 0.10;
            }
        }
        return 0.0;
    }

    private double computeFreshnessScore(String ticketingState, String showState) {
        if ("UPCOMING".equals(showState) && "IN_PROGRESS".equals(ticketingState)) {
            return 0.10;
        }
        if ("UPCOMING".equals(showState) && "BEFORE_OPEN".equals(ticketingState)) {
            return 0.06;
        }
        if ("ONGOING".equals(showState)) {
            return 0.04;
        }
        return 0.0;
    }

    private double computeTimingScore(Show show) {
        if (!"UPCOMING".equals(computeShowState(show))) {
            return 0.0;
        }

        long daysUntilShow = Duration.between(LocalDateTime.now(), show.getShowStartDate()).toDays();
        if (daysUntilShow < 0) {
            return 0.0;
        }
        if (daysUntilShow <= 7) {
            return 0.08;
        }
        if (daysUntilShow <= 14) {
            return 0.05;
        }
        if (daysUntilShow <= 30) {
            return 0.03;
        }
        return 0.0;
    }

    private String buildLocalReason(Show show, double textScore, double artistScore, double searchScore,
        double freshnessScore, double timingScore, boolean coldStart) {
        List<String> reasons = new ArrayList<>();

        if (artistScore >= 0.25) {
            reasons.add("동일 아티스트 선호가 반영됨");
        }
        if (textScore >= 0.20) {
            reasons.add("좋아요/구매 이력과 설명 맥락이 유사함");
        }
        if (searchScore > 0.0) {
            reasons.add("최근 검색어와 관련됨");
        }
        if (freshnessScore >= 0.06) {
            reasons.add("현재 예매 가능 상태");
        } else if (timingScore >= 0.05) {
            reasons.add("공연 일정이 가까움");
        }

        if (reasons.isEmpty() && coldStart) {
            return "사용자 이력이 부족해 현재 예매 가능한 공연을 추천합니다.";
        }
        if (reasons.isEmpty()) {
            return "기본 추천 로직으로 선정된 공연";
        }

        return String.join(" + ", reasons);
    }

    private Set<String> extractMeaningfulTokens(String rawText) {
        Set<String> tokens = new HashSet<>();
        for (String token : normalizeText(rawText).split("[^\\p{IsAlphabetic}\\p{IsDigit}]+")) {
            if (token.isBlank() || token.length() < 2 || token.chars().allMatch(Character::isDigit)) {
                continue;
            }
            tokens.add(token);
        }
        return tokens;
    }

    private String normalizeText(String rawText) {
        if (rawText == null) {
            return "";
        }
        return rawText.trim().toLowerCase(Locale.ROOT);
    }

    private String computeShowState(Show show) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(show.getShowStartDate())) {
            return "UPCOMING";
        }
        if (now.isAfter(show.getShowEndDate())) {
            return "ENDED";
        }
        return "ONGOING";
    }

    private String computeTicketingState(Show show) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(show.getReservationStartDate())) {
            return "BEFORE_OPEN";
        }
        if (now.isAfter(show.getReservationEndDate())) {
            return "CLOSED";
        }
        return "IN_PROGRESS";
    }

    private RecommendedShowItem toRecommendedShowItem(Show show, double score) {
        return new RecommendedShowItem(show.getId(), show.getTitle(), show.getPosterUrl(), show.getVenue().getName(),
            show.getPurchaseLimit(), show.getVenue().getRegion().getName(),
            new RecommendedShowItem.ShowPeriod(show.getShowStartDate().toLocalDate(),
                show.getShowEndDate().toLocalDate()),
            new RecommendedShowItem.ReservationPeriod(show.getReservationStartDate(), show.getReservationEndDate()),
            show.getStatus().name(), show.getArtist(), computeTicketingState(show), computeShowState(show),
            roundScore(score));
    }

    private double roundScore(double score) {
        return Math.round(score * 10000.0) / 10000.0;
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

    private record LocalRecommendation(Show show, double score, String reason) {
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
