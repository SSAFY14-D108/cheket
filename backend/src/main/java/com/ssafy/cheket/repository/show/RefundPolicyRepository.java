package com.ssafy.cheket.repository.show;

import com.ssafy.cheket.entity.refundpolicy.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    List<RefundPolicy> findByShowIdOrderByDaysRemainingDesc(Long showId);
}
