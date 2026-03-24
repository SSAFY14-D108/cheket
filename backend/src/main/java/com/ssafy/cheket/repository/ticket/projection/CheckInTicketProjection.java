package com.ssafy.cheket.repository.ticket.projection;

// 체크인 시 검증 앱에 보여줄 티켓 + 좌석 + 공연 정보
public interface CheckInTicketProjection {

    Long getTicketId();

    String getShowTitle();

    String getSectionName();

    String getSeatNo();

    String getGrade();

    String getVenueName();

    String getSessionDate();
}
