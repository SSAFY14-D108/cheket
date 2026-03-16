package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.response.GetBookingRateResponse;
import com.ssafy.cheket.dto.host.response.GetTotalSalesResponse;
import com.ssafy.cheket.service.host.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hosts/shows/{showId}/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/total-sales")
    @Operation(summary = "총 판매 금액 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetTotalSalesResponse>> getTotalSales(@AuthenticationPrincipal Long hostId,
        @PathVariable Long showId) {
        GetTotalSalesResponse response = dashboardService.getTotalSales(hostId, showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "총 판매 금액 조회 완료", response));
    }

    @GetMapping("/booking-rate")
    @Operation(summary = "예매율 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetBookingRateResponse>> getBookingRate(@AuthenticationPrincipal Long hostId,
        @PathVariable Long showId) {
        GetBookingRateResponse response = dashboardService.getBookingRate(hostId, showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "예매율 조회 완료", response));
    }
}
