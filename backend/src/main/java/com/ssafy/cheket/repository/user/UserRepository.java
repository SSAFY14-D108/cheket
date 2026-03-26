package com.ssafy.cheket.repository.user;

import com.ssafy.cheket.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일 중복 확인 - 회원가입 시 이미 가입된 이메일인지 체크 (탈퇴 유저 포함)
    boolean existsByEmail(String email);

    // 전화번호 중복 확인 - 1전화번호 = 1계정 강제 (탈퇴 유저 포함)
    boolean existsByPhoneNumber(String phoneNumber);

    // 이메일로 유저 조회 - 탈퇴 유저 포함, AuthRepository 대신 사용 (비밀번호 초기화 등)
    Optional<User> findByEmail(String email);

    // 이름 + 전화번호로 유저 조회 - 이메일 찾기에서 사용 (탈퇴 유저 포함, 현재 미사용)
    Optional<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);

    // 전화번호로 유저 조회 - 탈퇴 유저 포함 (현재 미사용)
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    // 탈퇴하지 않은 유저만 이메일로 조회 - 로그인 시 탈퇴 유저 차단
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // 탈퇴하지 않은 유저만 이름 + 전화번호로 조회 - 이메일 찾기 시 탈퇴 유저 제외
    Optional<User> findByUsernameAndPhoneNumberAndDeletedAtIsNull(String username, String phoneNumber);

    // 탈퇴하지 않은 유저만 ID로 조회 - 인증이 필요한 API에서 유저 존재 여부 확인 시 사용
    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    // 지갑 ID로 유저 조회 - 온체인 동기화 시 지갑 주소 → 사용자 매핑
    Optional<User> findByWalletId(Long walletId);
}
