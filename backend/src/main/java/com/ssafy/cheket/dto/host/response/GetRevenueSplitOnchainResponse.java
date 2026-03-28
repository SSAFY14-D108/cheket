package com.ssafy.cheket.dto.host.response;

import com.ssafy.cheket.enums.StakeholderRole;

import java.math.BigDecimal;
import java.util.List;

public record GetRevenueSplitOnchainResponse(Long showId, String title, BigDecimal totalRevenue,
    List<SplitOnchainInfo> splits) {

    public record SplitOnchainInfo(StakeholderRole role, Long id, String name, Integer rateBps, BigDecimal amount,
        OnchainInfo onchain) {
    }

    public record OnchainInfo(Long stakeholderNftId, String walletAddress, String onchainRole, Integer onchainShareBps,
        Long eventNftId) {
    }
}
