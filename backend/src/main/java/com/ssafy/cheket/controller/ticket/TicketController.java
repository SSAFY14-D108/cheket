package com.ssafy.cheket.controller.ticket;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.service.ticket.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/{ticketId}/refund")
    @Operation(summary = "티켓 환불")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> refund(@PathVariable Long ticketId) {
        ticketService.refundTicket(ticketId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "티켓 환불에 성공했습니다.", null));
    }

}
