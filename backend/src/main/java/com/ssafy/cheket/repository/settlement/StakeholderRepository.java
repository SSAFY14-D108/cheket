package com.ssafy.cheket.repository.settlement;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StakeholderRepository extends JpaRepository<Stakeholder, Long> {
    List<Stakeholder> findByShowId(Long showId);
}
