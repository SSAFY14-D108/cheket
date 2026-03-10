package com.ssafy.cheket.controller.show;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.show.response.*;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ShowSort;
import com.ssafy.cheket.service.show.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/shows")
public class ShowController {

    private final ShowService showService;

    @GetMapping
    @Operation(summary = "공연 목록 조회")
    public ResponseEntity<ApiResponse<GetShowListResponse>> getShowList(@RequestParam(required = false) Region region,
        @RequestParam(required = false) ShowSort sort, @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        GetShowListResponse response = showService.getShowList(region, sort, keyword, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 목록 조회 완료", response));
    }

    @GetMapping("/{showId}")
    @Operation(summary = "공연 상세 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetShowDetailResponse>> getShowDetail(@PathVariable Long showId) {
        GetShowDetailResponse response = showService.getShowDetail(showId);
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
}
