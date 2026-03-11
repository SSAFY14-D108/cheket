package com.ssafy.cheket.repository.ticket;

import com.ssafy.cheket.entity.ticket.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
