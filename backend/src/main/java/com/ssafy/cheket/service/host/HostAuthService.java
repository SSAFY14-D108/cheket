package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.request.LoginRequest;
import com.ssafy.cheket.dto.host.response.LoginResponse;

public interface HostAuthService {
    // 주최측 로그인
    LoginResponse hostLogin(LoginRequest request);

}
