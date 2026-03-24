package com.ssafy.cheket.service.notification;

import com.ssafy.cheket.dto.notification.request.CreateNotificationRequest;
import com.ssafy.cheket.entity.notification.Notification;
import com.ssafy.cheket.entity.notification.NotificationMessage;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.notification.NotificationMessageRepository;
import com.ssafy.cheket.repository.notification.NotificationRepository;
import com.ssafy.cheket.repository.user.UserRepository;
import com.ssafy.cheket.util.notification.NotificationTemplateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMessageRepository notificationMessageRepository;
    private final UserRepository userRepository;

    @Override
    public void createNotification(CreateNotificationRequest request) {
        if (!userRepository.existsById(request.getUserId()))
            throw new NotFoundException("존재하지 않는 사용자입니다.");

        NotificationMessage template = notificationMessageRepository.findByType(request.getType())
            .orElseThrow(() -> new NotFoundException("알림 메시지 템플릿을 찾을 수 없습니다."));

        String finalMessage = NotificationTemplateUtil.replaceVariables(template.getMessage(),
            getVariablesOrEmpty(request.getVariables()));

        Notification notification = Notification.builder().title(request.getTitle()).message(finalMessage)
            .userId(request.getUserId()).notificationMessageId(template.getId()).build();

        notificationRepository.save(notification);
    }

    private Map<String, String> getVariablesOrEmpty(Map<String, String> variables) {
        return variables == null ? Map.of() : variables;
    }
}
