package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.request.HostSignupRequest;
import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.dto.host.response.GetHostInfoResponse;

public interface HostService {

    // 주최측 회원가입
    void hostSignup(HostSignupRequest request) throws Exception;

    // 사업자 등록번호 중복 확인
    CheckBusinessNoDuplicateResponse checkBusinessNoDuplicate(String businessNo);

    // 회사 정보 조회
    GetHostInfoResponse getHostInfo(Long id);

}
