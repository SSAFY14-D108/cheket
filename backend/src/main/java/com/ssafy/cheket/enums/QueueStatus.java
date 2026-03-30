package com.ssafy.cheket.enums;

public enum QueueStatus {
    WAITING, // 대기 중
    ACTIVE, // 입장 가능 (아직 queue/enter 안 함)
    COMPLETED, // queue/enter 성공 후 좌석 페이지 진입 완료
    EXPIRED, // ACTIVE 시간 만료
    LEFT // 사용자가 대기열 이탈
}
