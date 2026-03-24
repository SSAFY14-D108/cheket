package com.ssafy.cheket.repository.notification;

import com.ssafy.cheket.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
