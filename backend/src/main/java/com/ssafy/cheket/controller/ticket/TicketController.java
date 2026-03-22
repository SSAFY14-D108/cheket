package com.ssafy.cheket.controller.ticket;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.ticket.request.PurchaseTicketRequest;
import com.ssafy.cheket.dto.ticket.request.TransferTicketRequest;
import com.ssafy.cheket.dto.ticket.response.GetUpcomingTicketResponse;
import com.ssafy.cheket.dto.ticket.response.GetUsedAndExpiredTicketResponse;
import com.ssafy.cheket.dto.ticket.response.PurchaseTicketResponse;
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
@RequestMapping("/api/v1")
public class TicketController {

    private final TicketService ticketService;

    /**
     * 티켓 구매 — 선택한 좌석에 대해 온체인 구매 처리
     *
     * [흐름] ① SSF.approve() — 사용자 Keystore로 대리 서명 ② PurchaseRouter.purchaseTicket()
     * — 플랫폼 키로 서명 ③ SSF: 구매자 → Settlement (자금 잠금) ④ NFT: 플랫폼 → 구매자 (소유권 이전)
     */
    @PostMapping("/shows/{showId}/sessions/{sessionId}/purchase")
    @Operation(summary = "티켓 구매", description = "선택한 좌석에 대해 SSF 결제 + NFT 소유권 이전")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PurchaseTicketResponse>> purchaseTickets(@AuthenticationPrincipal Long userId,
        @RequestHeader("Seat-Access-Token") String seatAccessToken, @PathVariable Long showId,
        @PathVariable Long sessionId, @RequestBody PurchaseTicketRequest request) {
        Long txId = ticketService.purchaseTickets(userId, showId, sessionId, seatAccessToken, request.sessionSeatIds());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok(200, "티켓 구매가 완료되었습니다.", new PurchaseTicketResponse(txId)));
    }

    @PostMapping("/tickets/{ticketId}/refund")
    @Operation(summary = "티켓 환불")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> refund(@PathVariable Long ticketId) {
        ticketService.refundTicket(ticketId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "티켓 환불에 성공했습니다.", null));
    }

    @GetMapping("/tickets/upcoming")
    @Operation(summary = "볼 예정인 티켓 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetUpcomingTicketResponse>>> getUpcomingTickets(
        @AuthenticationPrincipal Long userId) {
        List<GetUpcomingTicketResponse> response = ticketService.getUpcomingTickets(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "볼 예정인 티켓 목록 조회에 성공했습니다.", response));
    }

    @GetMapping("/tickets/collection")
    @Operation(summary = "관람 완료 및 만료 티켓 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetUsedAndExpiredTicketResponse>>> getUsedAndExpiredTickets(
        @AuthenticationPrincipal Long userId) {
        List<GetUsedAndExpiredTicketResponse> response = ticketService.getUsedAndExpiredTickets(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "티켓 컬렉션 목록 조회에 성공했습니다.", response));
    }

    // TicketController.java에 추가

    /**
     * 지정 양도 — 전화번호로 1:1 무료 양도
     *
     * 티켓 구매와 동일한 비동기 패턴: 즉시 txId 반환 → 백그라운드에서 온체인 처리 → 앱이 폴링
     */
    @PostMapping("/tickets/{ticketId}/transfer")
    @Operation(summary = "티켓 양도", description = "전화번호를 입력하여 티켓을 무료 양도")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PurchaseTicketResponse>> transferTicket(@AuthenticationPrincipal Long userId,
        @PathVariable Long ticketId, // 양도할 티켓 ID
        @RequestBody TransferTicketRequest request // { "phoneNumber": "010-1234-5678" }
    ) {
        Long txId = ticketService.transferTicket(userId, ticketId, request.phoneNumber());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok(200, "양도 요청이 접수되었습니다.", new PurchaseTicketResponse(txId)));
    }

}
