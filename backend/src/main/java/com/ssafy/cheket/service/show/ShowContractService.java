package com.ssafy.cheket.service.show;

import org.springframework.security.core.Authentication;

public interface ShowContractService {

    // 계약에 대한 승인
    void approve(Authentication authentication, Long userId, Long showId);

    // 계약에 대한 거절
    void reject(Authentication authentication, Long userId, Long showId);

}
