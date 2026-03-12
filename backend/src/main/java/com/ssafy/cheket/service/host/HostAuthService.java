package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.request.LoginRequest;
import com.ssafy.cheket.dto.host.response.LoginResponse;

public interface HostAuthService {
    // 주최측 로그인
    LoginResponse hostLogin(LoginRequest request);

    // 주최측 로그아웃
    // Access Token을 Redis 블랙리스트에 등록 -> 이후 해당 토큰으로 API 호출 시 401
    void hostLogout(String accessToken, String refreshToken);
}
