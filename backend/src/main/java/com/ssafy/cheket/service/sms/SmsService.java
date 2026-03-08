package com.ssafy.cheket.service.sms;

public interface SmsService {

    // 회원가입 시 필요한 인증번호 전송
    void sendVerificationCode(String phoneNumber);

    // 비밀번호 변경 시 필요한 인증번호 전송
    void sendPasswordResetVerificationCode(String email);

}
