package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.ShowItem;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.ticket.TicketEffect;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketEffectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostShowServiceImpl implements HostShowService {
    private final TicketEffectRepository ticketEffectRepository;
    private final ShowRepository showRepository;

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
