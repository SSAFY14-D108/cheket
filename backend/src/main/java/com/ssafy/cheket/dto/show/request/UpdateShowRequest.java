package com.ssafy.cheket.dto.show.request;

import com.ssafy.cheket.enums.StakeholderRole;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 공연 부분 수정 요청 DTO (PATCH) 모든 필드가 nullable → null이면 기존 값 유지, 값이 있으면 해당 필드만 수정
 */
public record UpdateShowRequest(
    // ── 공연 기본 정보 (null이면 수정 안 함) ──
    String title, // 공연명
    Long venueId, // 공연장 ID
    String artist, // 아티스트명
    String description, // 공연 설명
    Integer playtime, // 공연 시간(분)
    Integer purchaseLimit, // 1인당 구매 제한

    // ── 기간 설정 (null이면 수정 안 함) ──
    LocalDateTime showStartDate, // 공연 시작일
    LocalDateTime showEndDate, // 공연 종료일
    LocalDateTime reservationStartDate, // 예매 시작일시
    LocalDateTime reservationEndDate, // 예매 종료일시

    // ── 회차 목록 (null이면 수정 안 함, 빈 배열이면 전체 삭제) ──
    List<SessionInfo> sessionInfo,

    // ── 등급/가격 목록 (null이면 수정 안 함) ──
    List<GradeInfo> grade,

    // ── 환불 정책 목록 (null이면 수정 안 함) ──
    List<RefundPolicyInfo> refundPolicy,

    List<String> existingDescriptionImageUrls,

    List<StakeholderInfo> stakeholders) {

    public record SessionInfo(LocalDateTime sessionDate, // 회차 날짜
        LocalDateTime sessionStartTime // 시작 시간
    ) {
    }

    public record GradeInfo(String gradeName, // 등급명: "VIP"
        Integer price, // 가격: 150000
        String colorCode, // 색상: "#FF0000"
        List<Long> sectionIds, // 적용 구역 ID 목록
        Long ticketEffectId // 티켓 효과 ID (선택)
    ) {
    }

    public record RefundPolicyInfo(Integer daysRemaining, // 남은 일수: 7
        Integer refundRate // 환불률: 100(%)
    ) {
    }

    public record StakeholderInfo(StakeholderRole role, Long id, String name, String number, Integer shareBps) {
    }

}
