package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.GetBookingRateResponse;
import com.ssafy.cheket.dto.host.response.GetTotalSalesResponse;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
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
    private final SessionSeatRepository sessionSeatRepository;

    // 총 판매 금액 조회
    @Override
    public GetTotalSalesResponse getTotalSales(Long hostId, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 볼 수 있습니다.");

        Integer totalPrimarySales = ticketRepository.sumPrimarySalesByShowId(showId);
        if (totalPrimarySales == null)
            totalPrimarySales = 0;

        return new GetTotalSalesResponse(show.getId(), show.getTitle(), totalPrimarySales);
    }

    // 예매율 조회
    @Override
    public GetBookingRateResponse getBookingRate(Long hostId, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 볼 수 있습니다.");

        int capacity = sessionSeatRepository.countTotalSeatsByShowId(showId);
        int reservedSeats = ticketRepository.countReservedSeatsByShowId(showId);

        double bookingRate = 0.0;
        if (capacity > 0)
            bookingRate = Math.round(((double) reservedSeats / capacity * 100) * 100) / 100.0;

        return new GetBookingRateResponse(show.getId(), show.getTitle(), capacity, reservedSeats, bookingRate);
    }
}
