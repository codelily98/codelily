package com.codelily.backend.service;

import com.codelily.backend.config.JwtTokenProvider;
import com.codelily.backend.domain.User;
import com.codelily.backend.dto.LoginRequest;
import com.codelily.backend.dto.LoginResponse;
import com.codelily.backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;

    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    /**
     * 일반 로그인 (JWT 발급)
     * - AccessToken은 JSON으로 반환
     * - RefreshToken은 HttpOnly 쿠키에 저장
     */
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        // Access / Refresh 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Redis에 RefreshToken 저장
        refreshTokenService.storeRefresh(user.getId(), refreshToken, REFRESH_TTL);

        // RefreshToken 쿠키 설정
        attachRefreshCookie(response, refreshToken);

        return new LoginResponse(accessToken, user.getNickname(), user.getRole().name());
    }

    /**
     * Refresh 토큰을 이용한 Access 토큰 재발급 (회전 전략 적용)
     */
    public LoginResponse refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh 토큰이 유효하지 않습니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String storedRefresh = refreshTokenService.getRefresh(userId);

        // Redis에 저장된 Refresh 토큰과 다르면 회전 공격 방지 → 재발급 거부
        if (storedRefresh == null || !storedRefresh.equals(refreshToken)) {
            throw new IllegalArgumentException("Refresh 토큰이 만료되었거나 무효화되었습니다.");
        }

        // 새로운 Access / Refresh 토큰 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        // Redis에 새 Refresh 토큰 저장 (회전)
        refreshTokenService.storeRefresh(user.getId(), newRefreshToken, REFRESH_TTL);

        // 쿠키 갱신
        attachRefreshCookie(response, newRefreshToken);

        return new LoginResponse(newAccessToken, user.getNickname(), user.getRole().name());
    }

    /**
     * 로그아웃 처리
     * - Redis에서 Refresh 토큰 삭제
     * - Access 토큰은 블랙리스트에 등록 (만료 전까지 재사용 방지)
     */
    public void logout(String refreshToken, String accessToken) {
        // Redis에서 Refresh 토큰 제거
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenService.revokeRefresh(userId);
        }

        // Access 토큰을 블랙리스트에 등록
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Date exp = jwtTokenProvider.getExpiration(accessToken);
            long ttl = Math.max(1, (exp.getTime() - System.currentTimeMillis()) / 1000);
            String jti = jwtTokenProvider.getJti(accessToken);
            refreshTokenService.blacklist(jti != null ? jti : accessToken, Duration.ofSeconds(ttl));
        }
    }

    /**
     * RefreshToken을 HttpOnly 쿠키에 저장
     */
    private void attachRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // HTTPS 환경에서는 true로 설정
                .sameSite("Lax")
                .path("/")
                .maxAge(REFRESH_TTL)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    /**
     * RefreshToken 쿠키 삭제 메서드 (로그아웃 시 사용)
     */
    public void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // 배포 시 true
                .sameSite("Lax")
                .path("/")
                .maxAge(0) // 즉시 만료
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
