package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.dto.auth.request.LoginRequest;
import com.ssafy.cheket.dto.auth.request.ReissueRequest;
import com.ssafy.cheket.dto.auth.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse reissue(ReissueRequest request);


}
