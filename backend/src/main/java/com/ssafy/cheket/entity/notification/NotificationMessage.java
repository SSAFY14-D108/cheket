package com.ssafy.cheket.entity.notification;

import com.ssafy.cheket.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "notification_messages")
public class NotificationMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NotificationType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
}
