package com.ssafy.cheket.dto.wallet.response;

import java.time.LocalDateTime;

public record TransactionResponse(Long transactionId, String type, Long amount, String description, Long sellerId,
    Long buyerId, LocalDateTime createdAt) {
}
