package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.response.CreateShowResponse;
import com.ssafy.cheket.dto.host.response.GetHostShowDetailResponse;
import com.ssafy.cheket.dto.settlement.response.GetApprovalListResponse;
import com.ssafy.cheket.dto.show.request.AddShowRequest;
import com.ssafy.cheket.dto.show.request.UpdateShowRequest;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hosts/shows")
public class HostShowController {
    private final HostShowService hostShowService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "공연 등록")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CreateShowResponse>> createShow(@AuthenticationPrincipal Long hostId,
        @RequestPart("show") AddShowRequest request, @RequestPart("posterImage") MultipartFile posterImage,
        @RequestPart(value = "descriptionImages", required = false) List<MultipartFile> descriptionImages) {
        CreateShowResponse response = hostShowService.createShow(hostId, request, posterImage, descriptionImages);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "공연 등록 완료", response));
    }

    @PostMapping("/{showId}/contracts/confirm")
    @Operation(summary = "공연 최종 등록 (StakeholderNFT 발행)", description = "이해관계자 전원 승인 확인 후 StakeholderNFT 온체인 발행."
        + " 전원 APPROVED가 아니면 400 에러 반환.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Map<String, Long>>> confirmShow(@AuthenticationPrincipal Long hostId,
        @PathVariable Long showId) {
        Long txId = hostShowService.confirmShow(hostId, showId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(201, "최종 등록 완료 — StakeholderNFT 발행 진행 중", Map.of("showId", showId, "txId", txId)));
    }

    @PatchMapping(value = "/{showId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "공연 정보 부분 수정 (PATCH)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> updateShow(@AuthenticationPrincipal Long hostId, @PathVariable Long showId,
        @RequestPart("show") UpdateShowRequest request,
        @RequestPart(value = "posterImage", required = false) MultipartFile posterImage,
        @RequestPart(value = "descriptionImages", required = false) List<MultipartFile> descriptionImages) {
        hostShowService.updateShow(hostId, showId, request, posterImage, descriptionImages);
        return ResponseEntity.ok(ApiResponse.ok(200, "공연 수정 완료", null));
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
        @AuthenticationPrincipal Long loginId, Authentication authentication, @PathVariable Long showId) {
        String role = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
            .orElse(null);
        GetHostShowDetailResponse response = hostShowService.getHostShowDetail(loginId, role, showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 상세 조회 완료", response));
    }

    @DeleteMapping("/{showId}")
    @Operation(summary = "공연 삭제")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> deleteShow(@AuthenticationPrincipal Long hostId,
        @PathVariable Long showId) {
        hostShowService.deleteShow(hostId, showId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "공연 삭제 완료", null));
    }

    @GetMapping("/{showId}/contracts")
    @Operation(summary = "공연 별 스마트컨트랙트 승인/거절 내역 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetApprovalListResponse>>> getContracts(@AuthenticationPrincipal Long hostId,
        @PathVariable Long showId) {
        List<GetApprovalListResponse> response = hostShowService.getContracts(hostId, showId);
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponse.ok(200, "공연 스마트컨트랙트의 승인/거절에 관해 조회 성공했습니다.", response));
    }

}
