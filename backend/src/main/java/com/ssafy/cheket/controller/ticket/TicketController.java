package com.ssafy.cheket.controller.ticket;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.resale.request.CreateResaleRequest;
import com.ssafy.cheket.dto.ticket.request.PurchaseTicketRequest;
import com.ssafy.cheket.dto.ticket.request.TransferTicketRequest;
import com.ssafy.cheket.dto.ticket.response.*;
import com.ssafy.cheket.dto.common.TxIdResponse;
import com.ssafy.cheket.dto.ticket.request.CheckInRequest;
import com.ssafy.cheket.service.ticket.QrTokenService;
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
    private final QrTokenService qrTokenService;

    @PostMapping("/shows/{showId}/sessions/{sessionId}/purchase")
    @Operation(summary = "티켓 구매", description = "선택한 좌석에 대해 SSF 결제 + NFT 소유권 이전")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<TxIdResponse>> purchaseTickets(@AuthenticationPrincipal Long userId,
        @RequestHeader("Seat-Access-Token") String seatAccessToken, @PathVariable Long showId,
        @PathVariable Long sessionId, @RequestBody PurchaseTicketRequest request) {
        Long txId = ticketService.purchaseTickets(userId, showId, sessionId, seatAccessToken, request.sessionSeatIds());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok(200, "티켓 구매가 완료되었습니다.", new TxIdResponse(txId)));
    }

    @PostMapping("/tickets/{ticketId}/refund")
    @Operation(summary = "티켓 환불")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<TxIdResponse>> refund(@AuthenticationPrincipal Long userId,
        @PathVariable Long ticketId) {
        Long txId = ticketService.refundTicket(userId, ticketId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok(200, "티켓 환불 요청이 접수되었습니다.", new TxIdResponse(txId)));
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

    @GetMapping("/tickets/collection/onchain")
    @Operation(summary = "보관함 온체인 정보 포함 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetCollectionOnchainResponse>>> getCollectionWithOnchain(
        @AuthenticationPrincipal Long userId) {
        List<GetCollectionOnchainResponse> response = ticketService.getCollectionWithOnchain(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "보관함 온체인 정보 조회에 성공했습니다.", response));
    }

    @PostMapping("/tickets/{ticketId}/transfer")
    @Operation(summary = "티켓 양도", description = "전화번호를 입력하여 티켓을 무료 양도")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<TxIdResponse>> transferTicket(@AuthenticationPrincipal Long userId,
        @PathVariable Long ticketId, // 양도할 티켓 ID
        @RequestBody TransferTicketRequest request // { "phoneNumber": "010-1234-5678" }
    ) {
        Long txId = ticketService.transferTicket(userId, ticketId, request.phoneNumber());
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok(200, "양도 요청이 접수되었습니다.", new TxIdResponse(txId)));
    }

    @PostMapping("/tickets/{ticketId}/resales")
    @Operation(summary = "리세일 등록", description = "보유 티켓을 리세일 마켓에 등록 (원가 이하 온체인 강제)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<TxIdResponse>> createResale(@AuthenticationPrincipal Long userId,
        @PathVariable Long ticketId, @RequestBody CreateResaleRequest request) {
        Long txId = ticketService.createResale(userId, ticketId, request.resalePrice());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(201, "리세일 등록 요청이 접수되었습니다.", new TxIdResponse(txId)));
    }

    @PostMapping("/tickets/{ticketId}/qr")
    @Operation(summary = "QR 입장 토큰 발급", description = "티켓 입장용 QR 코드 데이터를 발급 (30초 유효)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<QrTokenResponse>> generateQrToken(@AuthenticationPrincipal Long userId,
        @PathVariable Long ticketId) {
        QrTokenResponse response = qrTokenService.generateQrToken(userId, ticketId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "QR 토큰이 발급되었습니다.", response));
    }

    @PostMapping("/tickets/check-in")
    @Operation(summary = "QR 입장 검증 + 체크인 (DB)", description = "검증 앱에서 QR 스캔 후 DB 기반 소유권 검증 + 체크인 처리")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkIn(@RequestBody CheckInRequest request) {
        CheckInResponse response = qrTokenService.verifyAndCheckIn(request.qrToken());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "입장이 확인되었습니다.", response));
    }

    @PostMapping("/tickets/check-in/onchain")
    @Operation(summary = "QR 입장 검증 + 체크인 (온체인)", description = "검증 앱에서 QR 스캔 후 온체인 소유권 검증 + 체크인 처리")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CheckInResponse>> checkInOnchain(@RequestBody CheckInRequest request) {
        CheckInResponse response = qrTokenService.verifyAndCheckInOnchain(request.qrToken());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "입장이 확인되었습니다.", response));
    }

}
