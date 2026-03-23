package com.ssafy.cheket.dto.resale.request;

/**
 * 리세일 등록 요청 DTO resalePrice만 받음 — deadline은 서버가 공연 시작 시각으로 자동 설정
 */
public record CreateResaleRequest(int resalePrice) {
}
