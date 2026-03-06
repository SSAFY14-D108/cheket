package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;

public interface HostService {

    // 사업자 등록번호 중복 확인
    CheckBusinessNoDuplicateResponse checkBusinessNoDuplicate(String businessNo);

}
