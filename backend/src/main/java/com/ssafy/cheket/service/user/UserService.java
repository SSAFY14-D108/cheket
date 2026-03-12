package com.ssafy.cheket.service.user;

import com.ssafy.cheket.dto.auth.request.FindEmailRequest;
import com.ssafy.cheket.dto.user.request.UserSignupRequest;
import com.ssafy.cheket.dto.auth.response.FindEmailResponse;

public interface UserService {
    void userSignup(UserSignupRequest request) throws Exception;
    // 이메일 찾기
    FindEmailResponse findEmail(FindEmailRequest request);
    // 탈퇴
    void withdrawUser(Long userId, String accessToken, String refreshToken);
}
