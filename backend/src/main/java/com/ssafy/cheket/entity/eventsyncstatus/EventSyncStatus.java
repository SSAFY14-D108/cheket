package com.ssafy.cheket.entity.eventsyncstatus;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 이벤트 리스너 동기화 상태 엔티티 — "체크포인트" 역할
 *
 * [왜 필요한가?] 이벤트 리스너(SsfTransferEventListener)는 5초마다 블록체인에서 Transfer 이벤트를 조회한다.
 * 만약 서버가 다운됐다가 재시작되면, "어디까지 읽었는지" 모르면 이벤트를 놓칠 수 있다. 이 테이블에 "마지막으로 처리한 블록 번호"를
 * 저장해두면, 서버 재시작 시 그 블록 다음부터 이어서 읽으면 된다. (체크포인트와 같은 개념)
 *
 * [테이블 구조] | id | listener_name | last_synced_block | updated_at |
 * |----|----------------|-------------------|---------------------| | 1 |
 * SSF_TRANSFER | 12345 | 2026-03-11 14:00:00 |
 *
 * listener_name이 UNIQUE이므로 리스너당 정확히 1개의 레코드만 존재한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "event_sync_status")
public class EventSyncStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리스너 고유 이름 (예: "SSF_TRANSFER") -> 체크포인트를 구분하기 위한 고유 이름표
    // 지금은 이벤트 리스너가 하나지만 나중에 다른 이벤트 리스너가 추가돼도, 각자 별도의 체크포인트를 가질 수 있음
    /**
     * event_sync_status 테이블: | id | listener_name | last_synced_block |
     * |----|------------------|-------------------| | 1 | SSF_TRANSFER | 12345 | ←
     * SSF 전송 이벤트 리스너 (각 리스너마다 어디까지 읽었는지 다를 수 있음) | 2 | NFT_TRANSFER | 12340 | ←
     * (미래) NFT 전송 이벤트 리스너 | 3 | TICKET_MINT | 12300 | ← (미래) 티켓 발행 이벤트 리스너
     */
    @Column(name = "listener_name", nullable = false, unique = true, length = 50)
    private String listenerName;

    // 마지막으로 처리 완료한 블록 번호
    // 다음 폴링 시 이 값 + 1 부터 조회를 시작함
    @Column(name = "last_synced_block", nullable = false)
    private Long lastSyncedBlock;

    // 마지막 업데이트 시각 (디버깅/모니터링 용도)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 최초 저장 시 updatedAt 자동 설정
    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 업데이트 시 updatedAt 자동 갱신
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
