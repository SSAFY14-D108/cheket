package com.ssafy.cheket.service.show;

import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowSort;
import com.ssafy.cheket.repository.show.ShowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;

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
