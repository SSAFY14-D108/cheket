package com.ssafy.cheket.config;

import com.ssafy.cheket.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 추가: 필터 주입을 위해
public class SecurityConfig {

    // JWT 필터 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 방식은 CSRF 방어 불필요)
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 사용 안 함 — JWT는 토큰 기반이라 서버에 세션 저장 불필요
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 API
                .requestMatchers("/api/v1/auth/**").permitAll() // 로그인, 로그아웃, SMS
                .requestMatchers("/api/v1/users/signup").permitAll() // 사용자 회원가입
                .requestMatchers("/api/v1/hosts/signup").permitAll() // 주최측 회원가입
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                // 나머지는 인증 필수
                .anyRequest().authenticated())

            // JWT 필터를 Spring Security 기본 인증 필터 앞에 배치
            // → 요청이 컨트롤러에 도달하기 전에 JWT 검증이 먼저 실행됨
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
