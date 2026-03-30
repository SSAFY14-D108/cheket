package com.ssafy.cheket.repository.resale;

import com.ssafy.cheket.entity.resale.Resale;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Resale 엔티티 저장/조회용 Repository
 */
public interface ResaleEntityRepository extends JpaRepository<Resale, Long> {

    java.util.Optional<Resale> findByTicketIdAndStatus(Long ticketId, Resale.ResaleListingStatus status);

    void deleteByTicketId(Long ticketId);

    /**
     * 종료된 회차의 ACTIVE 리세일 조회 (정산 시 만료 처리용) ticket → session_seat → session 조인
     */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT r.* FROM resales r
        JOIN tickets t ON r.ticket_id = t.id
        JOIN session_seats ss ON t.session_seat_id = ss.id
        WHERE ss.session_id = :sessionId
          AND r.status = 'ACTIVE'
        """, nativeQuery = true)
    java.util.List<Resale> findActiveResalesBySessionId(
        @org.springframework.data.repository.query.Param("sessionId") Long sessionId);
}
