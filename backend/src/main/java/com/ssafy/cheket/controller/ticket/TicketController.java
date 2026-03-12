package com.ssafy.cheket.controller.ticket;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;
import com.ssafy.cheket.service.ticket.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/upcoming")
    @Operation(summary = "볼 예정인 티켓 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetUpcomingTicketResponse>>> getUpcomingTickets(
        @AuthenticationPrincipal Long userId) {
        List<GetUpcomingTicketResponse> response = ticketService.getUpcomingTickets(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "볼 예정인 티켓 목록 조회에 성공했습니다.", response));
    }

}
