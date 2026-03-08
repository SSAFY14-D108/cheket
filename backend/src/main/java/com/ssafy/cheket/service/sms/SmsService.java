package com.ssafy.cheket.service.sms;

public interface SmsService {

    void sendVerificationCode(String phoneNumber);

}
