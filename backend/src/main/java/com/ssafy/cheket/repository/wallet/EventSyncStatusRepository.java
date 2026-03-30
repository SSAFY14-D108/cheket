package com.ssafy.cheket.repository.wallet;

import com.ssafy.cheket.entity.eventsyncstatus.EventSyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 이벤트 리스너 동기화 상태(체크포인트) Repository
 *
 * [사용 시나리오] SsfTransferEventListener가 매 폴링(5초)마다: 1.
 * findByListenerName("SSF_TRANSFER")로 마지막 처리 블록 조회 2. 그 블록 + 1 ~ 현재 최신 블록 범위로
 * eth_getLogs 호출 3. 처리 완료 후 lastSyncedBlock 업데이트 → save()
 *
 * 서버 재시작 시에도 이 값이 DB에 남아있으므로, 놓친 블록 없이 이어서 이벤트를 조회할 수 있다.
 */
public interface EventSyncStatusRepository extends JpaRepository<EventSyncStatus, Long> {

    /**
     * 리스너 이름으로 동기화 상태 조회
     *
     * @param listenerName
     *            리스너 고유 식별자 (예: "SSF_TRANSFER")
     * @return 해당 리스너의 마지막 동기화 블록 정보 - 첫 실행 시 Optional.empty() → 현재 블록-1로 초기화
     */
    Optional<EventSyncStatus> findByListenerName(String listenerName);
}
