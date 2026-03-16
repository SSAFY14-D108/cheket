package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.response.GetHostShowDetailResponse;
import com.ssafy.cheket.dto.show.request.AddShowRequest;
import com.ssafy.cheket.dto.show.response.GetShowListResponse;
import com.ssafy.cheket.dto.show.response.ShowItem;
import com.ssafy.cheket.dto.ticket.response.GetTicketEffectsResponse;
import com.ssafy.cheket.service.host.HostShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hosts/shows")
public class HostShowController {
    private final HostShowService hostShowService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "공연 등록")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Long>> createShow(@AuthenticationPrincipal Long hostId,
        @RequestPart("show") AddShowRequest request, @RequestPart("posterImage") MultipartFile posterImage,
        @RequestPart(value = "descriptionImages", required = false) List<MultipartFile> descriptionImages) {
        Long showId = hostShowService.createShow(hostId, request, posterImage, descriptionImages);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "공연 등록 완료", showId));
    }

    @GetMapping("/effect")
    @Operation(summary = "티켓 효과 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetTicketEffectsResponse>>> getTicketEffects() {
        List<GetTicketEffectsResponse> response = hostShowService.getTicketEffects();
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "티켓 효과 목록 조회 완료", response));
    }

    @GetMapping
    @Operation(summary = "내 공연 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetShowListResponse<ShowItem>>> getMyShows(@AuthenticationPrincipal Long hostId,
        @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        GetShowListResponse<ShowItem> response = hostShowService.getMyShows(hostId, page, size);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "내 공연 목록 조회 완료", response));
    }

    @GetMapping("/{showId}")
    @Operation(summary = "공연 상세 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetHostShowDetailResponse>> getHostShowDetail(
        @AuthenticationPrincipal Long hostId, @PathVariable Long showId) {
        GetHostShowDetailResponse response = hostShowService.getHostShowDetail(hostId, showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 상세 조회 완료", response));
    }
}
