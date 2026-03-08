package com.ssafy.cheket.controller.auth;

import com.ssafy.cheket.dto.auth.request.SmsSendForChangePasswordRequest;
import com.ssafy.cheket.dto.auth.request.SmsSendVerificationRequest;
import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.service.sms.SmsService;
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
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final SmsService smsService;

    @PostMapping("/sms/send")
    @Operation(summary = "회원가입 시 필요한 인증코드 전송")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody SmsSendVerificationRequest request) {
        smsService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "인증 코드가 전송되었습니다.", null));
    }

    @PostMapping("/password")
    @Operation(summary = "비밀번호 변경 시 필요한 인증코드 전송")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetVerificationCode(
        @RequestBody SmsSendForChangePasswordRequest request) {
        smsService.sendPasswordResetVerificationCode(request.email());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "인증 코드가 전송되었습니다.", null));
    }

}
