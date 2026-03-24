package com.ssafy.cheket.service.show;

public interface ShowContractService {

    // 계약에 대한 승인
    void approve(Long userId, Long showId);

}
