package com.ssafy.cheket.service.user;

import com.ssafy.cheket.dto.user.request.UserSignupRequest;

public interface UserService {
    void userSignup(UserSignupRequest request) throws Exception;

}
