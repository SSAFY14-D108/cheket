package com.ssafy.cheket.enums;

public enum ResaleStatus {
    AVAILABLE, // 구매 완료, 보유 중
    SOLD, // 구매 완료, 보유 중(단순 보유)
    LISTED, // 리세일 마켓에 등록 됨
    USED, // QR 체크인 완료
    EXPIRED // 공연 종료 후 미사용
}
