package com.ssafy.cheket.repository.notification;

import com.ssafy.cheket.entity.notification.NotificationMessage;
import com.ssafy.cheket.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationMessageRepository extends JpaRepository<NotificationMessage, Long> {
    Optional<NotificationMessage> findByType(NotificationType type);
}
