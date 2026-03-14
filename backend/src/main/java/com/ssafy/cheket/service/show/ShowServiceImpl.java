package com.ssafy.cheket.service.show;

import com.ssafy.cheket.config.s3.S3Uploader;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.entity.show.*;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowSort;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.*;
import com.ssafy.cheket.repository.show.projection.SessionListProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {

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
    private final S3Uploader s3Uploader;

    // 공연 검색 및 목록 조회
    @Override
    public GetShowListResponse<ShowItem> getShowList(Region region, ShowSort sort, String keyword, int page, int size) {
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        ShowSort normalizedSort = (sort == null) ? ShowSort.POPULAR : sort;

        Page<Show> result;
        if (normalizedSort == ShowSort.POPULAR) {
            Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
            result = showRepository.searchOrderByPopular(region, normalized, pageable);
        } else {
            Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100), toSort(normalizedSort));
            result = showRepository.search(region, normalized, pageable);
        }

        List<ShowItem> items = result.getContent().stream().map(this::toShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    private Sort toSort(ShowSort sort) {
        return switch (sort) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case DEADLINE -> Sort.by(Sort.Direction.ASC, "reservationEndDate");
            default -> throw new BadRequestException("지원하지 않는 정렬입니다.");
        };
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

        return new GetShowDetailResponse(show.getId(), show.getTitle(), show.getPosterUrl(), show.getVenue().getName(),
            show.getPurchaseLimit(), show.getVenue().getRegion(),
            new GetShowDetailResponse.ShowPeriod(show.getShowStartDate().toLocalDate(),
                show.getShowEndDate().toLocalDate()),
            new GetShowDetailResponse.ReservationPeriod(show.getReservationStartDate(), show.getReservationEndDate()),
            show.getStatus(), show.getDescription(), show.getArtist(), isLiked, likeCount, grades, refundPolicies);
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

    // 오른 예정 공연 5개 조회
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
        if(!showRepository.existsById(showId)) {
            throw new NotFoundException("해당 공연은 존재하지 않습니다.");
        }

        return showImageRepository.findAllByShow_Id(showId);
    }

    // 공연 이미지 삭제
    @Override
    public void deleteShowImages(Long showId) {
        s3Uploader.deleteAllByShowId(showId);
        showImageRepository.deleteAllByShow_Id(showId);
    }

    private ShowItem toShowItem(Show s) {
        return new ShowItem(s.getId(), s.getTitle(), s.getPosterUrl(), s.getVenue().getName(), s.getPurchaseLimit(),
            s.getVenue().getRegion().name(),
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
