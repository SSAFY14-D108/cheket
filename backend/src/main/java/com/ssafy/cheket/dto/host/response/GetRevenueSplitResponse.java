package com.ssafy.cheket.dto.host.response;

import com.ssafy.cheket.enums.StakeholderRole;

import java.math.BigDecimal;
import java.util.List;

public record GetRevenueSplitResponse(Long showId, String title, BigDecimal totalRevenue, List<SplitInfo> splits) {
    public record SplitInfo(StakeholderRole role, Long id, String name, Integer rateBps, BigDecimal amount) {
    }
}
