package com.ssafy.cheket.dto.common;

/**
 * 블록체인 TX 응답 DTO (공통) 구매, 양도, 리세일 등록 등 비동기 블록체인 처리 시 txId를 반환 클라이언트가 이 txId로
 * GET /api/v1/wallets/transactions/{txId} 폴링
 */
public record TxIdResponse(Long txId) {
}
