package com.ssafy.cheket.dto.wallet.response;

import java.time.LocalDateTime;

public record TransactionResponse(Long transactionId, String type, Long amount, String description, String txStatus,
    String txHash, Long blockNumber, Long sellerId, String sellerName, Long buyerId, String buyerName,
    LocalDateTime createdAt) {
}
