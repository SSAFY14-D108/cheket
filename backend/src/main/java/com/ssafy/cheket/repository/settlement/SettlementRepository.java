package com.ssafy.cheket.repository.settlement;

import com.ssafy.cheket.entity.settlement.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findBySessionId(Long sessionId);

    List<Settlement> findByShowId(Long showId);

    /**
     * 특정 유저 또는 호스트가 수령한 정산 이력 조회 stakeholder.user_id = :id (일반 유저) OR
     * stakeholder.host_id = :id (호스트)
     */
    @Query(value = """
        SELECT s.* FROM settlements s
        JOIN stakeholder st ON st.id = s.stakeholder_id
        WHERE st.user_id = :id OR st.host_id = :id
        ORDER BY s.created_at DESC
        """, nativeQuery = true)
    List<Settlement> findByUserIdOrHostId(@Param("id") Long id);
}
