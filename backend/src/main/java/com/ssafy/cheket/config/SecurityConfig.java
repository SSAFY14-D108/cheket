package com.ssafy.cheket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.cheket.config.jwt.JwtAuthenticationFilter;
import com.ssafy.cheket.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // 추가: 필터 주입을 위해
public class SecurityConfig {

    // JWT 필터 주입
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정 적용
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // CSRF 비활성화 (JWT 방식은 CSRF 방어 불필요)
            .csrf(AbstractHttpConfigurer::disable)

            // 세션 사용 안 함 — JWT는 토큰 기반이라 서버에 세션 저장 불필요
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 인증 없이 접근 가능한 API
                .requestMatchers("/api/v1/auth/**").permitAll() // 로그인, 로그아웃, SMS
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/hosts").permitAll().requestMatchers("/api/v1/hosts/auth/**")
                .permitAll() // 주최측 로그인, 로그아웃
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Swagger
                .requestMatchers("/collection/**").permitAll() // 컬렉션 WebView (인증 불필요)
                .requestMatchers("/api/v1/hosts/business-no/duplicate").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/resales").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/resales/shows/{showId}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shows").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shows/venue").permitAll()
                .requestMatchers("/api/v1/shows/{showId}/refund").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/shows/upcoming").permitAll()
                // 나머지는 인증 필수
                .anyRequest().authenticated())

            .exceptionHandling(eh -> eh.authenticationEntryPoint((req, res, ex) -> {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setCharacterEncoding("UTF-8");

                String body = objectMapper
                    .writeValueAsString(ApiResponse.fail(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다."));
                res.getWriter().write(body);
            }).accessDeniedHandler((req, res, ex) -> {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                res.setCharacterEncoding("UTF-8");

                String body = objectMapper
                    .writeValueAsString(ApiResponse.fail(HttpServletResponse.SC_FORBIDDEN, "접근 권한이 없습니다."));
                res.getWriter().write(body);
            }))

            // JWT 필터를 Spring Security 기본 인증 필터 앞에 배치
            // → 요청이 컨트롤러에 도달하기 전에 JWT 검증이 먼저 실행됨
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS 설정 — 허용할 출처, 메서드, 헤더 정의
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*"); // 모든 출처 허용 (추후 프론트 도메인만 허용으로 변경)
        config.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        config.addAllowedHeader("*"); // 모든 헤더 허용
        config.setAllowCredentials(true); // 쿠키/인증 헤더 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
