package com.ssafy.cheket.service.host;

import com.ssafy.cheket.dto.host.response.CheckBusinessNoDuplicateResponse;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.ConflictException;
import com.ssafy.cheket.repository.host.HostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HostServiceImpl implements HostService {

    private final HostRepository hostRepository;

    // 사업자 등록번호 확인 정규식
    private static final String REGEX = "^\\d{3}-\\d{2}-\\d{5}$";

    // 사업자 등록번호 중복 확인
    @Override
    public CheckBusinessNoDuplicateResponse checkBusinessNoDuplicate(String businessNo) {
        if (!businessNo.matches(REGEX)) {
            throw new BadRequestException("잘못된 사업자 등록번호 형식입니다.");
        }

        if (hostRepository.existsByBusinessNo(businessNo)) {
            throw new ConflictException("이미 등록된 사업자 등록번호입니다.");
        }

        return new CheckBusinessNoDuplicateResponse(false);
    }

}
