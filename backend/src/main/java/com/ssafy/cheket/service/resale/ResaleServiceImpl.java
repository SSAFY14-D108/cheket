package com.ssafy.cheket.service.resale;

import com.ssafy.cheket.dto.resale.response.ResaleShowItem;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ResaleShowSort;
import com.ssafy.cheket.repository.resale.ResaleRepository;
import com.ssafy.cheket.repository.resale.projection.ResaleShowProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResaleServiceImpl implements ResaleService {
    private final ResaleRepository resaleRepository;

    @Override
    public GetShowListResponse<ResaleShowItem> getResaleShowList(Region region, ResaleShowSort sort, String keyword,
        int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        ResaleShowSort normalizedSort = (sort == null) ? ResaleShowSort.POPULAR : sort;

        Page<ResaleShowProjection> result = switch (normalizedSort) {
            case DEADLINE -> resaleRepository.searchListedShowsOrderByDeadline(region, normalized, pageable);
            case POPULAR -> resaleRepository.searchListedShowsOrderByPopular(region, normalized,
                LocalDateTime.now().minusDays(7), pageable);
        };

        List<ResaleShowItem> items = result.getContent().stream().map(this::toResaleShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    private ResaleShowItem toResaleShowItem(ResaleShowProjection p) {
        return new ResaleShowItem(p.getShowId(), p.getTitle(), p.getShowStartDate().toLocalDate(),
            p.getShowEndDate().toLocalDate(), p.getVenue(), p.getRegion(), p.getPosterUrl(), p.getTicketCount());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
