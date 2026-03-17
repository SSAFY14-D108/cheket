package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.show.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    List<RefundPolicy> findByShowIdOrderByDaysRemainingDesc(Long showId);

    @Modifying
    @Query("DELETE FROM RefundPolicy rp WHERE rp.showId = :showId")
    void deleteAllByShowId(Long showId);
}
