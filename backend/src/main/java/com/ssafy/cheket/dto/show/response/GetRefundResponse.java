package com.ssafy.cheket.dto.show.response;

import java.time.LocalDate;
import java.util.List;

public record GetRefundResponse(List<RefundPolicyInfo> refundPolicy, LocalDate showStartDate) {
    public record RefundPolicyInfo(Integer daysRemaining, Integer refundRate) {
    }
}
