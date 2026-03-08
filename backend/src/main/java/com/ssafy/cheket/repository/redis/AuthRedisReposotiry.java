package com.ssafy.cheket.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthRedisReposotiry {

    // 회원가입 시 필요한 인증번호 prefix
    private static final String SMS_PREFIX = "auth:sms:";
    private static final String SMS_PREFIX_COOLDOWN_PREFIX = "auth:sms:cooldown:";

    // 비밀번호 변경 시 필요한 인증번호 prefix
    private static final String PASSWORD_RESET_SMS_PREFIX = "auth:password:reset:sms:";
    private static final String PASSWORD_RESET_SMS_COOLDOWN_PREFIX = "auth:password:reset:sms:cooldown:";

    private final StringRedisTemplate stringRedisTemplate;

    // 전화번호에 대한 SMS 인증번호 저장
    public void saveSmsVerificationCode(String phoneNumber, String verificationCode, Duration ttl) {
        String key = generateSmsCode(phoneNumber);
        stringRedisTemplate.opsForValue().set(key, verificationCode, ttl);
    }

    // 전화번호로 저장된 인증번호 조회
    public Optional<String> findSmsVerificationCode(String phoneNumber) {
        String key = generateSmsCode(phoneNumber);
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    // 입력한 인증번호와 redis에 저장된 인증번호가 일치하는지 확인 - 전화번호
    public boolean isSmsVerificationCodeMatched(String phoneNumber, String verificationCode) {
        return findSmsVerificationCode(phoneNumber).map(savedCode -> savedCode.equals(verificationCode)).orElse(false);
    }

    // 인증 완료 또는 만료 처리 시 redis의 인증번호 삭제(중복 인증 방지) - 전화번호
    public void deleteSmsVerificationCode(String phoneNumber) {
        String key = generateSmsCode(phoneNumber);
        stringRedisTemplate.delete(key);
    }

    // 해당 전화번호로 인증번호가 저장되어있는지 확인
    public boolean existsSmsVerificationCode(String phoneNumber) {
        String key = generateSmsCode(phoneNumber);
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // 해단 전화번호의 재요청 제한을 위한 cooldown 설정
    public void saveSmsCooldown(String phoneNumber, Duration ttl) {
        String key = generateSmsCooldown(phoneNumber);
        stringRedisTemplate.opsForValue().set(key, "cooldown", ttl);
    }

    // 해당 전화번호로 인즌번호 cooldown이 저장되어있는지 확인
    public boolean existsSmsCooldown(String phoneNumber) {
        String key = generateSmsCooldown(phoneNumber);
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // 비밀번호 변경 시 필요한 인증번호 저장
    public void savePasswordResetSmsCode(String email, String verificationCode, Duration ttl) {
        String key = generateSmsCode(email);
        stringRedisTemplate.opsForValue().set(key, verificationCode, ttl);
    }

    // 이메일로 저장된 인증코드 조회
    public Optional<String> findPasswordResetSmsCode(String email) {
        String key = generateSmsCode(email);
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    // 입력한 인증번호와 redis에 저장된 인증번호가 일치하는지 확인 - 이메일
    public boolean isPasswordResetSmsCodeMatched(String email, String verificationCode) {
        return findPasswordResetSmsCode(email).map(savedCode -> savedCode.equals(verificationCode)).orElse(false);
    }

    // 인증 완료 또는 만료 처리 시 redis의 인증번호 삭제(중복 인증 방지) - 이메일
    public void deletePasswordResetSmsCode(String email) {
        String key = generateSmsCode(email);
        stringRedisTemplate.delete(key);
    }

    // 해당 이메일로 인증번호가 저장되어있는지 확인
    public boolean existsPasswordResetSmsCode(String email) {
        String key = generateSmsCode(email);
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    // 해단 이메일의 재요청 제한을 위한 cooldown 설정
    public void savePasswordResetSmsCooldown(String email, Duration ttl) {
        String key = generateSmsCooldown(email);
        stringRedisTemplate.opsForValue().set(key, "cooldown", ttl);
    }

    // 해당 이메일로 인즌번호 cooldown이 저장되어있는지 확인
    public boolean existsPasswordResetSmsCooldown(String email) {
        String key = generateSmsCooldown(email);
        Boolean exists = stringRedisTemplate.hasKey(key);
        return Boolean.TRUE.equals(exists);
    }

    private String generateSmsCode(String phoneNumber) {
        return SMS_PREFIX + phoneNumber;
    }

    private String generateSmsCooldown(String phoneNumber) {
        return SMS_PREFIX_COOLDOWN_PREFIX + phoneNumber;
    }

    private String generatePasswordResetSmsCode(String email) {
        return PASSWORD_RESET_SMS_PREFIX + email;
    }

    private String generatePasswordResetSmsCooldown(String email) {
        return PASSWORD_RESET_SMS_COOLDOWN_PREFIX + email;
    }

}
