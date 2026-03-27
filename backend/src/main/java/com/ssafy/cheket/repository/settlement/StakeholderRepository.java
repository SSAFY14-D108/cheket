package com.ssafy.cheket.repository.settlement;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StakeholderRepository extends JpaRepository<Stakeholder, Long> {
    List<Stakeholder> findByShowId(Long showId);

    @Query("""
        select s
        from Stakeholder s
        where s.showId = :showId
          and (s.hostId is not null or s.userId is not null)
        order by s.id asc
        """)
    List<Stakeholder> findContractTargetsByShowId(@Param("showId") Long showId);

    Optional<Stakeholder> findByShowIdAndUserId(Long showId, Long userId);

    Optional<Stakeholder> findByShowIdAndHostId(Long showId, Long hostId);

    @Modifying
    @Query("DELETE FROM Stakeholder s WHERE s.showId = :showId")
    void deleteAllByShowId(Long showId);

    boolean existsByShowIdAndUserId(Long showId, Long loginId);

    // 탈퇴 블록: 종료되지 않은 공연의 APPROVED 이해관계자인지 확인
    @Query("""
        SELECT COUNT(s) > 0
        FROM Stakeholder s
        JOIN Show sh ON sh.id = s.showId
        WHERE s.userId = :userId
          AND s.approvalStatus = :status
          AND sh.showEndDate > :now
        """)
    boolean existsActiveApprovedStakeholderByUserId(
        @Param("userId") Long userId,
        @Param("status") ApprovalStatus status,
        @Param("now") LocalDateTime now
    );
}
