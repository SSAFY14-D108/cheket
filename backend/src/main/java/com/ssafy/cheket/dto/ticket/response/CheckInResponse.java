package com.ssafy.cheket.dto.ticket.response;

// 검증 앱에 보여줄 체크인 결과
public record CheckInResponse(boolean success, // 체크인 성공 여부
    Long ticketId, // 티켓 ID
    String userName, // 입장자 이름
    String showTitle, // 공연 제목
    String sectionName, // 구역명 (ex: "A구역")
    String seatNo, // 좌석번호 (ex: "3열 15번")
    String grade, // 좌석 등급 (ex: "VIP")
    String checkedInAt // 체크인 시각
) {
}
