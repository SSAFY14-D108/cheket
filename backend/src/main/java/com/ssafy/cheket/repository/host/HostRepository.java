package com.ssafy.cheket.repository.host;

import com.ssafy.cheket.entity.host.Host;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostRepository extends JpaRepository<Host, Long> {
    boolean existsByEmail(String email);
    boolean existsByBusinessNo(String businessNo);
    Optional<Host> findByBusinessNo(String businessNo);
    Optional<Host> findByEmail(String email);
    // 탈퇴하지 않은 호스트 조회 (로그인)
    Optional<Host> findByEmailAndDeletedAtIsNull(String email);
    // 탈퇴하지 않은 호스트 조회 (탈퇴, 정보조회, 정보수정)
    Optional<Host> findByIdAndDeletedAtIsNull(Long id);
    Optional<Host> findByBusinessNoAndDeletedAtIsNull(String businessNo);

}
