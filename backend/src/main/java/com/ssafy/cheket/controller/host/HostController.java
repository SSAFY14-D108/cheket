package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.auth.request.HostSignupRequest;
import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.service.host.HostService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<CheckBusinessNoDuplicateResponse>> checkBusinessNoDuplicate(String businessNo) {
        CheckBusinessNoDuplicateResponse response = hostService.checkBusinessNoDuplicate(businessNo);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "사용 가능한 사업자 등록번호 입니다.", response));
    }

}
