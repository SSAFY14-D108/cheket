package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.host.request.HostSignupRequest;
import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.request.ModifyHostInfoRequest;
import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.dto.host.response.GetHostInfoResponse;
import com.ssafy.cheket.service.host.HostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hosts")
public class HostController {

    private final HostService hostService;

    @PostMapping
    @Operation(summary = "주최측 회원가입")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody HostSignupRequest request) throws Exception {
        hostService.hostSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "회원 가입이 완료되었습니다.", null));
    }

    @PostMapping("/business-no/duplicate")
    @Operation(summary = "사업자 등록번호 중복 확인")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<CheckBusinessNoDuplicateResponse>> checkBusinessNoDuplicate(String businessNo) {
        CheckBusinessNoDuplicateResponse response = hostService.checkBusinessNoDuplicate(businessNo);
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

}
