package com.ssafy.cheket.dto.settlement.response;

import com.ssafy.cheket.enums.ApprovalStatus;
import com.ssafy.cheket.enums.UserType;

import java.time.LocalDateTime;

public record GetApprovalListResponse(UserType userType, Long userId, String username, ApprovalStatus approvalStatus,
    LocalDateTime determinedAt) {
}
