package com.ssafy.cheket.controller.host;

import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.host.request.LoginRequest;
import com.ssafy.cheket.dto.host.response.LoginResponse;
import com.ssafy.cheket.service.host.HostAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hosts/auth")
public class HostAuthController {
    private final HostAuthService hostAuthService;

    @PostMapping("/login")
    @Operation(summary = "주최측 로그인")
    public ResponseEntity<ApiResponse<LoginResponse>> hostLogin(@RequestBody LoginRequest request) {
        LoginResponse response = hostAuthService.hostLogin(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그인 성공", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "주최측 로그아웃")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> hostLogout(@RequestHeader("Authorization") String authorization) {
        String accessToken = authorization.replace("Bearer ", "");
        hostAuthService.hostLogout(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그아웃 완료", null));
    }

}
