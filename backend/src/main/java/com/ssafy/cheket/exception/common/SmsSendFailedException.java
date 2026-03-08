package com.ssafy.cheket.exception.common;

public class SmsSendFailedException extends RuntimeException {
    public SmsSendFailedException(String message) {
        super(message);
    }
}
