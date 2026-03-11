package com.ssafy.cheket.controller.resale;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.resale.response.GetResaleTicketsResponse;
import com.ssafy.cheket.dto.resale.response.ResaleShowItem;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.enums.Region;
import com.ssafy.cheket.enums.ResaleShowSort;
import com.ssafy.cheket.enums.ResaleTicketSort;
import com.ssafy.cheket.service.resale.ResaleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resales")
public class ResaleController {
    private final ResaleService resaleService;

    @GetMapping
    @Operation(summary = "2차 거래 티켓이 존재하는 공연 목록 조회")
    public ResponseEntity<ApiResponse<GetShowListResponse<ResaleShowItem>>> getResaleShowList(
        @RequestParam(required = false) Region region, @RequestParam(required = false) ResaleShowSort sort,
        @RequestParam(required = false) String keyword, @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
        GetShowListResponse<ResaleShowItem> response = resaleService.getResaleShowList(region, sort, keyword, page,
            size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "거래소 조회 완료", response));
    }

    @GetMapping("/shows/{showId}")
    @Operation(summary = "특정 공연의 2차거래 티켓 목록 조회")
    public ResponseEntity<ApiResponse<GetResaleTicketsResponse>> getResaleTickets(@PathVariable Long showId,
        @RequestParam(required = false) ResaleTicketSort sort, @RequestParam(required = false) Long sessionId,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        GetResaleTicketsResponse response = resaleService.getResaleTickets(showId, sort, sessionId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "거래 가능한 티켓 목록 조회 완료", response));
    }
}
