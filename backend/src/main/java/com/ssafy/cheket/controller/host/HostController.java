package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.host.request.BusinessNoDuplicateRequest;
import com.ssafy.cheket.dto.host.request.HostSignupRequest;
import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.request.HostWithdrawRequest;
import com.ssafy.cheket.dto.host.request.ModifyHostInfoRequest;
import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.dto.host.response.GetHostInfoResponse;
import com.ssafy.cheket.dto.show.response.GetSectionsResponse;
import com.ssafy.cheket.service.host.HostService;
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
@RequestMapping("/api/v1/hosts")
public class HostController {

    private final HostService hostService;
    private final ShowService showService;

    @PostMapping
    @Operation(summary = "주최측 회원가입")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody HostSignupRequest request) throws Exception {
        hostService.hostSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "회원 가입이 완료되었습니다.", null));
    }

    @PostMapping("/business-no/duplicate")
    @Operation(summary = "사업자 등록번호 중복 확인")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CheckBusinessNoDuplicateResponse>> checkBusinessNoDuplicate(
        BusinessNoDuplicateRequest request) {
        CheckBusinessNoDuplicateResponse response = hostService.checkBusinessNoDuplicate(request.businessNo());
        String message = response.isDuplicated() ? "이미 등록된 사업자 등록번호입니다." : "사용 가능한 사업자 등록번호입니다.";
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, message, response));
    }

    @GetMapping
    @Operation(summary = "회사 정보 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetHostInfoResponse>> getHostInfo(@AuthenticationPrincipal Long id) {
        GetHostInfoResponse response = hostService.getHostInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "조회에 성공했습니다.", response));
    }

    @PutMapping
    @Operation(summary = "회사 정보 수정")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> modifyHostInfo(@AuthenticationPrincipal Long id,
        @RequestBody ModifyHostInfoRequest request) {
        hostService.modifyHostInfo(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "회사 정보가 수정되었습니다.", null));
    }

    @GetMapping("/venues/{venueId}/sections")
    @Operation(summary = "구역 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<List<GetSectionsResponse>>> getSections(@PathVariable Long venueId) {
        List<GetSectionsResponse> response = showService.getSections(venueId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "구역 목록 조회 완료", response));
    }

    @DeleteMapping
    @Operation(summary = "주최측 회원 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> withdrawHost(@AuthenticationPrincipal Long id,
        @RequestHeader("Authorization") String authHeader,
        @RequestHeader(value = "Refresh-Token", required = false) String refreshToken,
        @RequestBody HostWithdrawRequest request) {
        String accessToken = authHeader.substring(7);
        hostService.withdrawHost(id, request.password(), accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "회원 탈퇴 완료", null));
    }

}
