package com.ssafy.cheket.service.ticket;

import com.ssafy.cheket.entity.show.RefundPolicy;
import com.ssafy.cheket.entity.show.Session;
import com.ssafy.cheket.entity.show.SessionSeat;
import com.ssafy.cheket.entity.ticket.Ticket;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.show.RefundPolicyRepository;
import com.ssafy.cheket.repository.show.SessionRepository;
import com.ssafy.cheket.repository.show.SessionSeatRepository;
import com.ssafy.cheket.repository.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final SessionSeatRepository sessionSeatRepository;
    private final SessionRepository sessionRepository;
    private final RefundPolicyRepository refundPolicyRepository;

    // 티켓 환불
    @Override
    public void refundTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new NotFoundException("유효하지 않은 티켓입니다."));

        if (ticket.getResaleStatus().equals(Ticket.ResaleStatus.EXPIRED)) {
            throw new NotFoundException("유효하지 않은 티켓입니다.");
        }

        SessionSeat sessionSeat = sessionSeatRepository.findById(ticket.getSessionSeatId())
            .orElseThrow(() -> new NotFoundException("유효하지 않은 티켓입니다."));
        Session session = sessionRepository.findById(sessionSeat.getSessionId())
            .orElseThrow(() -> new NotFoundException("유효하지 않은 티켓입니다."));
        List<RefundPolicy> refundPolicies = refundPolicyRepository
            .findByShowIdOrderByDaysRemainingDesc(session.getShowId());

        LocalDate now = LocalDate.now(ZoneId.of("Asia/Seoul"));
        long remain = ChronoUnit.DAYS.between(now, session.getSessionDate());
        for (RefundPolicy policy : refundPolicies) {
            if (policy.getDaysRemaining() > remain) {
                continue;
            }

            // TODO: 환불 로직 작성
            // 1. 티켓의 소유자를 현재 소유자 -> 서버측으로 변경 시도(실패 시 에러)
            // 2. 소유자 변경 완료 시 원래 소유자의 지갑으로 환불 금액 만큼의 가격 환불
            // 3. 해당 NFT의 소유자를 서버 운영자 측으로 변경
            // 4. DB에서 Ticket 삭제
            // 5. SessionSeat의 STATUS를 SOLD -> AVAILABLE로 변경
            // 6. 모든 로직을 완료하면 return 해줘야 다음 로직을 이행하지 않음.
            return;
        }
    }

}
