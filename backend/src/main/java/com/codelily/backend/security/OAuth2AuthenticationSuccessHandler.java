package com.codelily.backend.security;

import com.codelily.backend.config.JwtTokenProvider;
import com.codelily.backend.domain.User;
import com.codelily.backend.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // 프론트로 리디렉트할 URL (로컬 개발용)
    private static final String FRONT_REDIRECT = "http://localhost:3000/auth/callback";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User principal = (OAuth2User) authentication.getPrincipal();

        // CustomOAuth2UserService에서 넣어둔 userId 추출
        Long userId = principal.getAttribute("userId");
        if (userId == null) {
            // fallback: 이메일로 조회 가능
            String email = principal.getAttribute("email");
            if (email != null) {
                userId = userRepository.findByEmail(email).map(User::getId).orElse(null);
            }
        }

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유저 식별에 실패했습니다.");
            return;
        }

        // 권한 추출
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER")
                .replace("ROLE_", "");

        // JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);

        // Refresh 토큰은 HttpOnly Cookie로
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // 배포 시 true + HTTPS
                .path("/")
                .sameSite("Lax")
                .maxAge(7 * 24 * 3600)
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        // Access 토큰은 쿼리로 전달(또는 헤더로 전달해도 됨)
        String redirectUrl = FRONT_REDIRECT + "?accessToken=" + accessToken;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
