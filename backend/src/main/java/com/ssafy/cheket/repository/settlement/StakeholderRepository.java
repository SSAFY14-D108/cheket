package com.ssafy.cheket.repository.settlement;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StakeholderRepository extends JpaRepository<Stakeholder, Long> {
    List<Stakeholder> findByShowId(Long showId);

    @Modifying
    @Query("DELETE FROM Stakeholder s WHERE s.showId = :showId")
    void deleteAllByShowId(Long showId);
}
