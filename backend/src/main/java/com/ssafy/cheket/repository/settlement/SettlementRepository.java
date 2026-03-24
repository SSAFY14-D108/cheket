package com.ssafy.cheket.repository.settlement;

import com.ssafy.cheket.entity.settlement.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findBySessionId(Long sessionId);

    List<Settlement> findByShowId(Long showId);
}
