package com.ssafy.cheket.controller.user;

import com.ssafy.cheket.dto.auth.request.FindEmailRequest;
import com.ssafy.cheket.dto.user.request.UpdateNotificationRequest;
import com.ssafy.cheket.dto.user.request.UserSignupRequest;
import com.ssafy.cheket.dto.auth.response.FindEmailResponse;
import com.ssafy.cheket.dto.common.ApiResponse;
import com.ssafy.cheket.dto.user.response.GetProfileResponse;
import com.ssafy.cheket.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "회원가입") // Swagger 문서 자동 생성
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody UserSignupRequest request) throws Exception {
        userService.userSignup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(201, "회원가입 완료", null));
    }

    @PostMapping("/email")
    @Operation(summary = "이메일 찾기")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<FindEmailResponse>> findEmail(@RequestBody FindEmailRequest request) {
        FindEmailResponse response = userService.findEmail(request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "이메일 찾기에 성공했습니다.", response));
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> withdrawUser(Authentication authentication,
        @RequestHeader("Authorization") String authHeader,
        @RequestHeader(value = "Refresh-Token", required = false) String refreshToken) {
        Long userId = Long.parseLong(authentication.getName());
        String accessToken = authHeader.substring(7);
        userService.withdrawUser(userId, accessToken, refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(200, "회원 탈퇴 완료", null));
    }

    @GetMapping
    @Operation(summary = "프로필 조회")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<GetProfileResponse>> getProfile(@AuthenticationPrincipal Long userId) {
        GetProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "프로필 조회 완료", response));
    }

    @PutMapping("/notifications")
    @Operation(summary = "알림 여부 수정")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<Void>> updateNotification(@RequestBody UpdateNotificationRequest request,
        @AuthenticationPrincipal Long userId) {
        userService.updateNotification(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.ok(200, "알림 여부 수정 완료", null));
    }
}
