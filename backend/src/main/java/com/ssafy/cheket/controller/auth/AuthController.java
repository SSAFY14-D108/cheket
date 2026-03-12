package com.ssafy.cheket.controller.auth;

import com.ssafy.cheket.dto.auth.request.*;
import com.ssafy.cheket.dto.auth.response.LoginResponse;
import com.ssafy.cheket.dto.auth.response.SearchUserResponse;
import com.ssafy.cheket.dto.auth.response.SmsVerificationResponse;
import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.service.auth.AuthService;
import com.ssafy.cheket.service.sms.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SmsService smsService;

    @PostMapping("/login")
    @Operation(summary = "사용자 로그인")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그인 성공", response));
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급")
    public ResponseEntity<ApiResponse<LoginResponse>> reissue(@RequestBody ReissueRequest request) {
        LoginResponse response = authService.reissue(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "토큰 재발급 성공", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "사용자 로그아웃")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String authorization,
        @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        String accessToken = authorization.replace("Bearer ", "");
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "로그아웃 완료", null));
    }

    @PostMapping("/sms/send")
    @Operation(summary = "회원가입 시 필요한 인증코드 전송")
    public ResponseEntity<ApiResponse<Void>> sendVerificationCode(@RequestBody SmsSendVerificationRequest request) {
        smsService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "인증 코드가 전송되었습니다.", null));
    }

    @PostMapping("/password")
    @Operation(summary = "비밀번호 초기화 시 필요한 인증코드 전송")
    public ResponseEntity<ApiResponse<Void>> sendPasswordResetVerificationCode(
        @RequestBody SmsSendForChangePasswordRequest request) {
        smsService.sendPasswordResetVerificationCode(request.email());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "인증 코드가 전송되었습니다.", null));
    }

    @PostMapping("/sms/verify")
    @Operation(summary = "회원가입 시 발급 받은 인증코드 검증")
    public ResponseEntity<ApiResponse<SmsVerificationResponse>> verifySmsCode(
        @RequestBody SmsVerificationRequest request) {
        SmsVerificationResponse response = new SmsVerificationResponse(
            smsService.verifySmsCode(request.phoneNumber(), request.code()));
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "인증이 완료되었습니다.", response));
    }

    @PostMapping("/duplicate")
    @Operation(summary = "이메일 중복확인")
    public ResponseEntity<ApiResponse<Void>> checkEmailDuplicated(@RequestBody DuplicatedEmailCheckRequest request) {
        authService.checkEmailDuplicated(request.email());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "회원가입이 가능한 이메일입니다.", null));
    }

    @PatchMapping("/reset-password")
    @Operation(summary = "비밀번호 초기화")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.phoneNumber(), request.code(), request.newPassword());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "비밀번호가 변경되었습니다.", null));
    }

    @PatchMapping("/change-password")
    @Operation(summary = "비밀번호 변경")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal Long userId,
        @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request.oldPassword(), request.newPassword());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "비밀번호가 변경되었습니다.", null));
    }

    @GetMapping("/search")
    @Operation(summary = "회원 검색")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<SearchUserResponse>> searchUser(@RequestParam String userType,
        @RequestParam String number) {
        SearchUserResponse response = authService.searchUser(userType, number);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "조회에 성공했습니다.", response));
    }

}
