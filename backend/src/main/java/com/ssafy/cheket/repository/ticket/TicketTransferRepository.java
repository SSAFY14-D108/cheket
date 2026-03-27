package com.ssafy.cheket.repository.ticket;

import com.ssafy.cheket.entity.ticket.TicketTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketTransferRepository extends JpaRepository<TicketTransfer, Long> {

    void deleteByTicketId(Long ticketId);
}
