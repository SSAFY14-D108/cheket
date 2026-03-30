package com.ssafy.cheket.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        // ===== 1단계: 헤더에서 토큰 꺼내기 =====
        // "Authorization: Bearer eyJhbGci..." → "eyJhbGci..." 부분만 추출
        String token = resolveToken(request);

        // ===== 2단계: 토큰 검증 =====
        // 토큰이 있고, 유효한 경우에만 진행
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // ===== 3단계: 블랙리스트 확인 =====
            // Redis에 "blacklist: eyJhbGci..." 키가 있는지 확인
            Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + token);

            if (Boolean.TRUE.equals(isBlacklisted)) {
                // 블랙리스트에 있음 = 로그아웃된 토큰
                // 인증 정보를 설정하지 않고 그냥 넘김
                // -> Spring Security가 "인증 안 된 사용자"로 판단
                // -> authenticated() API 접근 시 401 에러
                filterChain.doFilter(request, response);
                return; // 여기서 끝임. 아래 코드 실행 안 함
            }

            // ===== 4단계: 인증 정보 설정 =====
            // 블랙리스트에 없으면 → 정상 토큰 → 인증 처리
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            // Spring Security가 이해하는 인증 객체 생성
            // (userId, 비밀번호, 권한 목록)
            // 비밀번호 null -> 이미 JWT로 인증했으니 필요 없음
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, // principal:
                                                                                                                 // 누구인지
                null, // credentials: 비밀번호 (불필요)
                List.of(new SimpleGrantedAuthority("ROLE_" + role)) // authorities: 권한 (ROLE_USER)
            );

            // Spring Security의 "인증 저장소"에 등록
            // → 이후 컨트롤러에서 "이 사용자는 인증됨"으로 인식
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // ===== 5단계: 다음 필터로 넘기기 =====
        // 이 줄이 없으면 요청이 컨트롤러까지 도달하지 못함
        filterChain.doFilter(request, response);

    }

    // ===== 헤더에서 토큰 추출하는 메서드 =====
    // "Authorization: Bearer eyJhbGci..." → "eyJhbGci..."
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7); // "Bearer " = 7글자 → 그 이후부터 추출
        }
        return null;
    }
}
