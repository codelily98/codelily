package com.codelily.backend.controller;

import com.codelily.backend.dto.LoginRequest;
import com.codelily.backend.dto.LoginResponse;
import com.codelily.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${oauth2.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.kakao.logout-redirect-uri}")
    private String kakaoLogoutRedirectUri;

    @Value("${oauth2.google.logout-redirect-uri}")
    private String googleLogoutRedirectUri;

    /**
     * [POST] 일반 로그인 (JWT 발급)
     * - AccessToken: JSON 반환
     * - RefreshToken: HttpOnly 쿠키 저장
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    /**
     * [POST] AccessToken 재발급 (RefreshToken 회전 전략 적용)
     * - RefreshToken: HttpOnly 쿠키로 자동 갱신
     * - AccessToken: JSON 반환
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }

    /**
     * [POST] 로그아웃
     * - Redis에서 RefreshToken 제거
     * - AccessToken은 블랙리스트에 등록 → 만료 전까지 재사용 차단
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            HttpServletResponse response
    ) {
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        authService.logout(refreshToken, accessToken);
        authService.clearRefreshCookie(response); // Refresh 쿠키 제거

        return ResponseEntity.ok().build();
    }

    /**
     * [GET] 카카오/구글 OAuth2 로그인 진입점
     * 예: /api/auth/oauth2/authorize/google
     * 예: /api/auth/oauth2/authorize/kakao
     */
    @GetMapping("/oauth2/authorize/{provider}")
    public void oauth2Authorize(
            @PathVariable("provider") String provider,
            HttpServletResponse response
    ) throws Exception {
        String redirectUrl = "/oauth2/authorization/" + provider;
        response.sendRedirect(redirectUrl);
    }

    /**
     * [GET] OAuth2 로그인 성공 콜백
     * - OAuth2AuthenticationSuccessHandler에서 JWT 발급 및 리디렉트 처리
     * - 프론트엔드에서는 /auth/callback 페이지에서 AccessToken을 받아 저장
     */
    @GetMapping("/oauth2/success")
    public ResponseEntity<String> oauth2Success() {
        return ResponseEntity.ok("소셜 로그인 성공 - 프론트 콜백에서 처리하세요");
    }
    
    /**
     * OAuth2 제공자 세션까지 만료 로그아웃
     * 예: /api/auth/oauth2/authorize/logout?provider=kakao
     * 예: /api/auth/oauth2/authorize/logout?provider=google
     */
    @GetMapping("/oauth2/logout")
    public void oauth2Logout(
            @RequestParam("provider") String provider,
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            HttpServletResponse response
    ) throws Exception {
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        // 1. 서버 측 JWT 무효화
        authService.logout(refreshToken, accessToken);
        authService.clearRefreshCookie(response);

        // 2. 소셜 제공자 세션 로그아웃
        if ("kakao".equalsIgnoreCase(provider)) {
            String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout" +
                    "?client_id=" + kakaoClientId +
                    "&logout_redirect_uri=" + kakaoLogoutRedirectUri;
            response.sendRedirect(kakaoLogoutUrl);
        } else if ("google".equalsIgnoreCase(provider)) {
            if (accessToken == null) {
                response.sendRedirect(googleLogoutRedirectUri);
            } else {
                String googleLogoutUrl = "https://accounts.google.com/o/oauth2/revoke?token=" + accessToken;
                response.sendRedirect(googleLogoutUrl);
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다.");
        }
    }
}
