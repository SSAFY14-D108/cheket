package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.host.request.HostSignupRequest;
import com.ssafy.cheket.dto.common.ApiResponse;
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
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "사용 가능한 사업자 등록번호 입니다.", response));
    }

    @GetMapping
    @Operation(summary = "회사 정보 조회")
    public ResponseEntity<ApiResponse<GetHostInfoResponse>> getHostInfo(@AuthenticationPrincipal Long id) {
        GetHostInfoResponse response = hostService.getHostInfo(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "조회에 성공했습니다.", response));
    }

}
