package com.ssafy.cheket.service.sms;

import com.ssafy.cheket.entity.user.User;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.GoneException;
import com.ssafy.cheket.exception.common.SmsSendFailedException;
import com.ssafy.cheket.exception.common.TooManyRequestsException;
import com.ssafy.cheket.repository.auth.AuthRepository;
import com.ssafy.cheket.repository.redis.AuthRedisReposotiry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private static final Pattern PHONENUMBER = Pattern.compile("^010\\d{8}$");

    private static final Duration SMS_CODE_TTL = Duration.ofMinutes(5);
    private static final Duration SMS_COOLDOWN_TTL = Duration.ofMinutes(5);

    private final AuthRedisReposotiry authRedisRepository;
    private final AuthRepository authRepository;

    @Value("${solapi.api-key}")
    private String apiKey;

    @Value("${solapi.api-secret}")
    private String apiSecret;

    @Value("${solapi.sender}")
    private String sender;

    // 회원가입 시 필요한 인증번호 발송
    @Override
    public void sendVerificationCode(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BadRequestException("전화번호는 필수입니다.");
        }

        if (!PHONENUMBER.matcher(phoneNumber).matches()) {
            throw new BadRequestException("잘못된 전화번호 형식입니다.");
        }

        if (authRedisRepository.existsSmsCooldown(phoneNumber)) {
            throw new TooManyRequestsException("인증 요청이 너무 잦습니다. 잠시 후 다시 시도해주세요.");
        }

        String verificationCode = generateVerificationCode();

        sendSms(phoneNumber, verificationCode);

        authRedisRepository.saveSmsVerificationCode(phoneNumber, verificationCode, SMS_CODE_TTL);
        authRedisRepository.saveSmsCooldown(phoneNumber, SMS_COOLDOWN_TTL);
    }

    // 비밀번호 변경 시 필요한 인증번호 전송
    @Override
    public void sendPasswordResetVerificationCode(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일은 필수입니다.");
        }

        User user = authRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("존재하지 않는 이메일입니다."));

        if (authRedisRepository.existsPasswordResetSmsCooldown(email)) {
            throw new TooManyRequestsException("인증 요청이 너무 잦습니다. 잠시 후 다시 시도해주세요.");
        }

        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BadRequestException("등록된 전화번호가 없습니다.");
        }

        String verificationCode = generateVerificationCode();

        sendSms(phoneNumber, verificationCode);

        authRedisRepository.savePasswordResetSmsCode(email, verificationCode, SMS_CODE_TTL);
        authRedisRepository.savePasswordResetSmsCooldown(email, SMS_COOLDOWN_TTL);
    }

    // 회원가입 시 발급 받은 인증코드 검증
    @Override
    public boolean verifySmsCode(String phoneNumber, String code) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new BadRequestException("전화번호는 필수입니다.");
        }

        if (code == null || code.isBlank()) {
            throw new BadRequestException("인증 코드는 필수입니다.");
        }

        if (!authRedisRepository.existsSmsVerificationCode(phoneNumber)) {
            throw new GoneException("인증 코드가 만료되었습니다.");
        }

        if (!authRedisRepository.isSmsVerificationCodeMatched(phoneNumber, code)) {
            throw new BadRequestException("인증 코드가 일치하지 않습니다.");
        }

        return true;
    }

    private String generateVerificationCode() {
        int number = new Random().nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private void sendSms(String to, String verificationCode) {
        try {
            DefaultMessageService messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret,
                "https://api.coolsms.co.kr");

            Message message = new Message();
            message.setFrom(sender);
            message.setTo(to);
            message.setText("[CHEKET] 인증번호는 [ " + verificationCode + " ] 입니다.");

            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));

            if (response == null) {
                throw new SmsSendFailedException("SMS 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
            }
        } catch (SmsSendFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new SmsSendFailedException("SMS 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

}
