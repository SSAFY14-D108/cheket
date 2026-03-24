package com.ssafy.cheket.service.user;

import com.ssafy.cheket.dto.auth.request.FindEmailRequest;
import com.ssafy.cheket.dto.notification.response.GetNotificationsResponse;
import com.ssafy.cheket.dto.user.request.SaveFcmTokenRequest;
import com.ssafy.cheket.dto.user.request.UpdateNotificationRequest;
import com.ssafy.cheket.dto.user.request.UserSignupRequest;
import com.ssafy.cheket.dto.auth.response.FindEmailResponse;
import com.ssafy.cheket.dto.user.response.GetProfileResponse;

import java.util.List;

public interface UserService {
    void userSignup(UserSignupRequest request);
    // 이메일 찾기
    FindEmailResponse findEmail(FindEmailRequest request);
    // 탈퇴
    void withdrawUser(Long userId, String accessToken, String refreshToken);

    GetProfileResponse getProfile(Long userId);

    void updateNotification(Long userId, UpdateNotificationRequest request);

    void saveFcmToken(Long userId, SaveFcmTokenRequest request);

    List<GetNotificationsResponse> getNotifications(Long userId);

    void readNotification(Long userId, Long notificationId);
}
