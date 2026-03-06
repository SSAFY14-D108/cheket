package com.ssafy.cheket.service.auth;

import com.ssafy.cheket.dto.auth.request.SignupRequest;

public interface AuthService {
    void signup(SignupRequest request) throws Exception;
}
