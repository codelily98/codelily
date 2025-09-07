package com.codelily.backend.config;

import com.codelily.backend.service.CustomUserDetailsService;
import com.codelily.backend.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Access 토큰을 검증하여 Spring Security 컨텍스트에 인증 정보를 저장하는 필터.
 * Refresh 토큰 검증은 AuthService에서 처리하며, Access 토큰만 검증합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Authorization 헤더에서 Bearer 토큰 추출
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String accessToken = authHeader.substring(7);

            // 2. Access 토큰 유효성 검사
            if (!jwtTokenProvider.validateToken(accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 3. 블랙리스트 토큰 여부 검사 (로그아웃 시 등록됨)
            String jti = jwtTokenProvider.getJti(accessToken);
            if (refreshTokenService.isBlacklisted(jti != null ? jti : accessToken)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4. 사용자 ID 추출
            Long userId = jwtTokenProvider.getUserId(accessToken);

            // 5. 이미 SecurityContext가 세팅되어 있으면 스킵
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 6. DB에서 사용자 정보 로드
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);

                // 7. Spring Security 인증 객체 생성 및 컨텍스트에 등록
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            // 예외 발생 시 인증을 중단하고 로그 남기기
        	logger.error("JWT 인증 필터 오류", e);
        }

        // 8. 다음 필터로 체인 진행
        filterChain.doFilter(request, response);
    }
}
