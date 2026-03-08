package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.GetShowDetailResponse;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.SessionListResponse;
import com.ssafy.cheket.entity.seatgrade.SeatGrade;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowSort;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.*;
import com.ssafy.cheket.repository.show.projection.SessionListProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

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

    // 공연 검색 및 목록 조회
    @Override
    public GetShowListResponse getShowList(Region region, ShowSort sort, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100), toSort(sort));

        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<Show> result = showRepository.search(region, normalized, pageable);

        List<GetShowListResponse.ShowItem> items = result.getContent().stream()
            .map(s -> new GetShowListResponse.ShowItem(s.getId(), s.getTitle(), s.getPosterUrl(),
                s.getVenue().getName(), s.getPurchaseLimit(), s.getVenue().getRegion().name(),
                new GetShowListResponse.ShowPeriod(s.getShowStartDate().toLocalDate(),
                    s.getShowEndDate().toLocalDate()),
                new GetShowListResponse.ReservationPeriod(s.getReservationStartDate(), s.getReservationEndDate()),
                s.getStatus().name()))
            .toList();

        return new GetShowListResponse(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    // 공연 상세 조회
    @Override
    public GetShowDetailResponse getShowDetail(Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        int likeCount = likeRepository.countByShowId(showId);

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

        // JWT 관련 추가된 후에 isLiked 수정하기
        return new GetShowDetailResponse(show.getId(), show.getTitle(), show.getPosterUrl(), show.getVenue().getName(),
            show.getPurchaseLimit(), show.getVenue().getRegion(),
            new GetShowDetailResponse.ShowPeriod(show.getShowStartDate().toLocalDate(),
                show.getShowEndDate().toLocalDate()),
            new GetShowDetailResponse.ReservationPeriod(show.getReservationStartDate(), show.getReservationEndDate()),
            show.getStatus(), show.getDescription(), show.getArtist(), false, likeCount, grades, refundPolicies);
    }

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

    private Sort toSort(ShowSort sort) {
        if (sort == null)
            sort = ShowSort.LATEST;

        return switch (sort) {
            case LATEST -> Sort.by(Sort.Direction.DESC, "createdAt");
            case DEADLINE -> Sort.by(Sort.Direction.ASC, "reservationEndDate");
            case POPULAR ->
                // TODO: 예매수 -> 티켓 엔티티 만들고 수정
                Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
