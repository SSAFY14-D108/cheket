package com.ssafy.cheket.repository.auth;

import com.ssafy.cheket.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findById(long id);

    Optional<User> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

}
