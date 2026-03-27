package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.GetBookingRateResponse;
import com.ssafy.cheket.dto.host.response.GetReservationsResponse;
import com.ssafy.cheket.dto.host.response.GetRevenueSplitResponse;
import com.ssafy.cheket.dto.host.response.GetTotalSalesResponse;
import com.ssafy.cheket.entity.host.Host;
import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.Show;
import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.enums.StakeholderRole;
import com.ssafy.cheket.exception.common.ForbiddenException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.host.HostRepository;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import com.ssafy.cheket.repository.show.SeatRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.show.ShowRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ShowRepository showRepository;
    private final TicketRepository ticketRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final StakeholderRepository stakeholderRepository;
    private final HostRepository hostRepository;
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;

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

    // 수입 배분 조회
    @Override
    public GetRevenueSplitResponse getRevenueSplit(Long loginId, String role, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        boolean canAccess = false;

        if ("ROLE_HOST".equals(role)) {
            canAccess = show.getHost().getId().equals(loginId);
        } else if ("ROLE_USER".equals(role)) {
            canAccess = stakeholderRepository.existsByShowIdAndUserId(showId, loginId);
        }

        if (!canAccess)
            throw new ForbiddenException("권한이 없습니다.");

        Integer totalPrimarySales = ticketRepository.sumPrimarySalesByShowId(showId);
        if (totalPrimarySales == null)
            totalPrimarySales = 0;

        BigDecimal totalRevenue = BigDecimal.valueOf(totalPrimarySales);

        List<Stakeholder> stakeholders = stakeholderRepository.findByShowId(showId);

        List<Long> hostIds = stakeholders.stream()
            .filter(stakeholder -> stakeholder.getRole() == StakeholderRole.ORGANIZER).map(Stakeholder::getHostId)
            .filter(Objects::nonNull).distinct().toList();

        List<Long> userIds = stakeholders.stream()
            .filter(stakeholder -> stakeholder.getRole() == StakeholderRole.ARTIST).map(Stakeholder::getUserId)
            .filter(Objects::nonNull).distinct().toList();

        Map<Long, Host> hostMap = hostIds.isEmpty()
            ? Collections.emptyMap()
            : hostRepository.findAllById(hostIds).stream().collect(Collectors.toMap(Host::getId, Function.identity()));

        Map<Long, User> userMap = userIds.isEmpty()
            ? Collections.emptyMap()
            : userRepository.findAllById(userIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<GetRevenueSplitResponse.SplitInfo> splits = stakeholders.stream().map(stakeholder -> {
            Long id = null;
            String name = null;

            if (stakeholder.getRole() == StakeholderRole.ORGANIZER) {
                id = stakeholder.getHostId();
                Host host = hostMap.get(id);
                name = host != null ? host.getCompanyName() : null;
            } else if (stakeholder.getRole() == StakeholderRole.ARTIST) {
                id = stakeholder.getUserId();
                User user = userMap.get(id);
                name = user != null ? user.getUsername() : null;
            }

            BigDecimal amount = totalRevenue.multiply(BigDecimal.valueOf(stakeholder.getShareBps()))
                .divide(BigDecimal.valueOf(10000), 3, RoundingMode.HALF_UP);

            return new GetRevenueSplitResponse.SplitInfo(stakeholder.getRole(), id, name, stakeholder.getShareBps(),
                amount);
        }).toList();

        return new GetRevenueSplitResponse(show.getId(), show.getTitle(), totalRevenue, splits);
    }

    // 회차별 예매 현황 조회
    @Override
    public GetReservationsResponse getReservations(Long hostId, Long showId) {
        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("존재하지 않는 공연입니다."));

        if (!show.getHost().getId().equals(hostId))
            throw new ForbiddenException("본인이 등록한 공연만 볼 수 있습니다.");

        List<Session> sessions = sessionRepository.findByShowIdOrderBySessionDateAsc(showId);

        int capacity = Math.toIntExact(seatRepository.countByShowId(showId));

        List<GetReservationsResponse.SessionInfo> sessionInfos = sessions.stream().map(session -> {
            int reservedSeats = sessionSeatRepository.countReservedSeatsBySessionId(session.getId());

            return new GetReservationsResponse.SessionInfo(session.getId(), session.getSessionDate().toLocalDate(),
                capacity, reservedSeats);
        }).toList();

        return new GetReservationsResponse(show.getId(), show.getTitle(), show.getVenue().getName(), sessionInfos);
    }
}
