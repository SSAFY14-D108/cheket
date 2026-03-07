package com.ssafy.cheket.repository.user;

import com.ssafy.cheket.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email); // 이메일 중복 확인 (회원가입 시 이미 가입된 이메일인지 체크)
    boolean existsByPhoneNumber(String phoneNumber); // 전화번호 중복 확인 (1전화번호 = 1계정 강제)
    Optional<User> findByEmail(String email); // 이메일로 사용자 조회 (로그인 시 해당 유저 찾기)

    Optional<User> findByUsernameAndPhoneNumber(String username, String phoneNumber);

}
