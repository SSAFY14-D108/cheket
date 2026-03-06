package com.ssafy.cheket.entity.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter // 모든 필드 getter 자동 생성
@Setter // 모든 필드 setter 자동 생성
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드 생성자
@Entity // JPA 엔티티 선언
@Builder // 빌더 패턴 사용 가능
@Table(name = "users") // users 테이블과 매핑
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "notification_enable", nullable = false)
    private Boolean notificationEnable;

    @PrePersist // INSERT 직전 실행
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // 생성 시간 자동 세팅
        this.updatedAt = LocalDateTime.now(); // 수정 시간도 초기화
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(); // 수정 시간 자동 갱신
    }
}
