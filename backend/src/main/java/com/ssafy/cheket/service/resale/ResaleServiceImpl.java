package com.ssafy.cheket.service.resale;

import com.ssafy.cheket.dto.resale.response.GetResaleTicketsResponse;
import com.ssafy.cheket.dto.resale.response.ResaleShowItem;
import com.ssafy.cheket.dto.resale.response.ResaleTicketItem;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.enums.ResaleShowSort;
import com.ssafy.cheket.enums.ResaleTicketSort;
import com.ssafy.cheket.repository.resale.ResaleRepository;
import com.ssafy.cheket.repository.resale.projection.ResaleShowProjection;
import com.ssafy.cheket.repository.resale.projection.ResaleTicketProjection;
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

    // 2차 거래 티켓이 존재하는 공연 목록 조회
    @Override
    public GetShowListResponse<ResaleShowItem> getResaleShowList(List<Integer> regions, ResaleShowSort sort,
        String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        List<Integer> normalizedRegions = (regions == null) ? List.of() : regions;
        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        ResaleShowSort normalizedSort = (sort == null) ? ResaleShowSort.POPULAR : sort;

        Page<ResaleShowProjection> result = switch (normalizedSort) {
            case DEADLINE -> resaleRepository.searchListedShowsOrderByDeadline(normalizedRegions,
                (long) normalizedRegions.size(), normalized, pageable);
            case POPULAR -> resaleRepository.searchListedShowsOrderByPopular(normalizedRegions,
                (long) normalizedRegions.size(), normalized, LocalDateTime.now().minusDays(7), pageable);
        };

        List<ResaleShowItem> items = result.getContent().stream().map(this::toResaleShowItem).toList();

        return new GetShowListResponse<>(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    // 특정 공연의 2차거래 티켓 목록 조회
    @Override
    public GetResaleTicketsResponse getResaleTickets(Long showId, ResaleTicketSort sort, Long sessionId, int page,
        int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), clamp(size, 1, 100));
        ResaleTicketSort normalizedSort = (sort == null) ? ResaleTicketSort.LATEST : sort;

        Page<ResaleTicketProjection> result = switch (normalizedSort) {
            case LATEST -> resaleRepository.findListedTicketsByShowOrderByLatest(showId, sessionId, pageable);
            case PRICE -> resaleRepository.findListedTicketsByShowOrderByPrice(showId, sessionId, pageable);
        };

        List<ResaleTicketItem> items = result.getContent().stream().map(this::toResaleTicketItem).toList();

        return new GetResaleTicketsResponse(items, result.getNumber(), result.getSize(), result.getTotalElements(),
            result.getTotalPages());
    }

    private ResaleTicketItem toResaleTicketItem(ResaleTicketProjection p) {
        return new ResaleTicketItem(p.getTicketId(),
            new ResaleTicketItem.SessionInfo(p.getSessionId(), p.getSessionDate().toLocalDate(),
                p.getSessionStartTime().toLocalTime()),
            p.getSectionName(), p.getSeatId(), p.getSeatNo(), p.getGrade(), p.getOriginalPrice(),
            p.getDiscountedPrice(), p.getDiscountRate());
    }

    private ResaleShowItem toResaleShowItem(ResaleShowProjection p) {
        return new ResaleShowItem(p.getShowId(), p.getTitle(), p.getShowStartDate().toLocalDate(),
            p.getShowEndDate().toLocalDate(), p.getVenue(), p.getRegion(), p.getPosterUrl(), p.getTicketCount());
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
