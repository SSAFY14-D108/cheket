package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.dto.auth.request.LoginRequest;
import com.ssafy.cheket.dto.auth.request.ReissueRequest;
import com.ssafy.cheket.dto.auth.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse reissue(ReissueRequest request);
    void logout(String accessToken);

    // 이메일 중복 확인
    void checkEmailDuplicated(String email);

    // 비밀번호 초기화
    void resetPassword(String phoneNumber, String code, String newPassword);

    // 비밀번호 변경
    void changePassword(Long id, String oldPassword, String newPassword);

}
