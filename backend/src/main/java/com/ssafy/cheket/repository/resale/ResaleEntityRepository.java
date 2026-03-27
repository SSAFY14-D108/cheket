package com.ssafy.cheket.repository.resale;

import com.ssafy.cheket.entity.resale.Resale;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Resale 엔티티 저장/조회용 Repository
 */
public interface ResaleEntityRepository extends JpaRepository<Resale, Long> {

    java.util.Optional<Resale> findByTicketIdAndStatus(Long ticketId, Resale.ResaleListingStatus status);

    void deleteByTicketId(Long ticketId);
}
