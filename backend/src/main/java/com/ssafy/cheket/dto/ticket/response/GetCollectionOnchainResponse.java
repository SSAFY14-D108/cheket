package com.ssafy.cheket.dto.ticket.response;

import java.time.LocalDateTime;

public record GetCollectionOnchainResponse(
    // DB 정보
    Long ticketId, String numbering, String posterUrl, ShowInfo show, Long seatId, String sectionName, String seatNo,
    String grade,
    // 온체인 정보
    OnchainInfo onchain) {
    public record ShowInfo(Long showId, String name, LocalDateTime date, String venue, String effect) {
    }

    public record OnchainInfo(Long tokenId, String ownerAddress, String tokenURI, String onchainStatus, // VALID, USED,
                                                                                                        // EXPIRED,
                                                                                                        // REFUNDED
        Long price, Long mintedAt // Unix timestamp
    ) {
    }
}
