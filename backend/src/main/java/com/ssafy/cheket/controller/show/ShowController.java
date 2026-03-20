package com.ssafy.cheket.controller.show;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.show.request.PurchaseSessionSeatRequest;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.enums.ShowSort;
import com.ssafy.cheket.service.show.ShowService;
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
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final ShowService showService;

    @GetMapping
    @Operation(summary = "공연 목록 조회")
    public ResponseEntity<ApiResponse<GetShowListResponse<ShowItem>>> getShowList(
        @RequestParam(required = false) List<Integer> regions, @RequestParam(required = false) ShowSort sort,
        @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        GetShowListResponse<ShowItem> response = showService.getShowList(regions, sort, keyword, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 목록 조회 완료", response));
    }

    @GetMapping("/{showId}")
    @Operation(summary = "공연 상세 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetShowDetailResponse>> getShowDetail(@PathVariable Long showId,
        @AuthenticationPrincipal Long userId) {
        GetShowDetailResponse response = showService.getShowDetail(showId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 상세 조회 완료", response));
    }

    @GetMapping("/{showId}/sessions")
    @Operation(summary = "회차 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<SessionListResponse>>> getSessionList(@PathVariable Long showId) {
        List<SessionListResponse> response = showService.getSessionList(showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "회차 목록 조회 완료", response));
    }

    @GetMapping("/{showId}/sessions/{sessionId}/seats")
    @Operation(summary = "좌석 배치도 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetSeatsResponse>>> getSeats(@PathVariable Long showId,
        @PathVariable Long sessionId) {
        List<GetSeatsResponse> response = showService.getSeats(showId, sessionId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "좌석 배치도 조회 완료", response));
    }

    @GetMapping("/venue")
    @Operation(summary = "공연장 목록 조회")
    public ResponseEntity<ApiResponse<List<GetVenuesResponse>>> getVenues() {
        List<GetVenuesResponse> response = showService.getVenues();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연장 목록 조회 완료", response));
    }

    @GetMapping("/{showId}/refund")
    @Operation(summary = "환불 정책 조회")
    public ResponseEntity<ApiResponse<GetRefundResponse>> getRefund(@PathVariable Long showId) {
        GetRefundResponse response = showService.getRefund(showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "환불 정책 조회 완료", response));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "오픈 예정 공연 5개 조회")
    public ResponseEntity<ApiResponse<GetUpcomingResponse>> getUpcoming() {
        GetUpcomingResponse response = showService.getUpcoming();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "오픈 예정 공연 5개 조회 완료", response));
    }

    @PostMapping("/{showId}/sessions/{sessionId}/seats")
    @Operation(summary = "좌석 선점(결제하기 버튼)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<PurchaseSessionSeatResponse>> purchaseSessionSeat(@PathVariable Long showId,
        @PathVariable Long sessionId, @RequestBody PurchaseSessionSeatRequest request) {
        PurchaseSessionSeatResponse response = showService.purchaseSessionSeats(showId, sessionId, request);
        String message = response.seats().failure().isEmpty() ? "좌석 선점 완료" : "이미 선택된 좌석입니다.";
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, message, response));
    }

}
