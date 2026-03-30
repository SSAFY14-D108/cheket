package com.ssafy.cheket.service.show;

import com.ssafy.cheket.entity.settlement.Stakeholder;
import com.ssafy.cheket.enums.ApprovalStatus;
import com.ssafy.cheket.exception.common.BadRequestException;
import com.ssafy.cheket.exception.common.NotFoundException;
import com.ssafy.cheket.repository.settlement.StakeholderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public void approve(Authentication authentication, Long userId, Long showId) {
        String role = getRole(authentication);

        if (role == null || role.isEmpty()) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        Stakeholder stakeholder = null;
        if (role.equals("USER")) {
            stakeholder = stakeholderRepository.findByShowIdAndUserId(showId, userId)
                .orElseThrow(() -> new NotFoundException("찾을 수 없는 공연입니다."));
        } else if (role.equals("HOST")) {
            stakeholder = stakeholderRepository.findByShowIdAndHostId(showId, userId)
                .orElseThrow(() -> new NotFoundException("찾을 수 없는 공연입니다."));
        }

        if (stakeholder == null) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        stakeholder.setApprovalStatus(ApprovalStatus.APPROVED);
        stakeholder.setApprovedAt(LocalDateTime.now());
        stakeholder.setRejectedAt(null);
    }

    @Override
    @Transactional
    public void reject(Authentication authentication, Long userId, Long showId) {
        String role = getRole(authentication);

        if (role == null || role.isEmpty()) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        Stakeholder stakeholder = null;
        if (role.equals("USER")) {
            stakeholder = stakeholderRepository.findByShowIdAndUserId(showId, userId)
                .orElseThrow(() -> new NotFoundException("찾을 수 없는 공연입니다."));
        } else if (role.equals("HOST")) {
            stakeholder = stakeholderRepository.findByShowIdAndHostId(showId, userId)
                .orElseThrow(() -> new NotFoundException("찾을 수 없는 공연입니다."));
        }

        if (stakeholder == null) {
            throw new BadRequestException("잘못된 요청입니다.");
        }

        stakeholder.setApprovalStatus(ApprovalStatus.REJECTED);
        stakeholder.setApprovedAt(null);
        stakeholder.setRejectedAt(LocalDateTime.now());
    }

    private String getRole(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).findFirst()
            .map(auth -> auth.replace("ROLE_", "")).orElse(null);
    }

}
