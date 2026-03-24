package com.ssafy.cheket.service.show;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.enums.ApprovalStatus;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShowContractServiceImpl implements ShowContractService {

    private final StakeholderRepository stakeholderRepository;

    @Override
    @Transactional
    public void approve(Long userId, Long showId) {
        Stakeholder stakeholder = stakeholderRepository.findByShowIdAndUserId(showId, userId)
            .or(() -> stakeholderRepository.findByShowIdAndHostId(showId, userId))
            .orElseThrow(() -> new NotFoundException("찾을 수 없는 공연입니다."));

        stakeholder.setApprovalStatus(ApprovalStatus.APPROVED);
        stakeholder.setApprovedAt(LocalDateTime.now());
        stakeholder.setRejectedAt(null);
    }

}
