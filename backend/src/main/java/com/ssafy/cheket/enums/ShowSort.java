package com.ssafy.cheket.enums;

public enum ShowSort {
    POPULAR, // 인기순(예매수)
    LATEST, // 최신순
    DEADLINE, // 마감임박순(예약 종료일 기준, 마감 안된 공연만)
    OPEN_SOON // 오픈임박순(예약 시작일 기준, 오픈 안된 공연만)
}
