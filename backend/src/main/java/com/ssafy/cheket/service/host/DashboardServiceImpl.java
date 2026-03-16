package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.GetTotalSales;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;

    @Override
    public GetTotalSales getTotalSales(Long hostId, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 볼 수 있습니다.");

        Integer totalPrimarySales = ticketRepository.sumPrimarySalesByShowId(showId);
        if (totalPrimarySales == null)
            totalPrimarySales = 0;

        return new GetTotalSales(show.getId(), show.getTitle(), totalPrimarySales);
    }
}
