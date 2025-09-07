package com.codelily.backend.security;

import com.codelily.backend.domain.User;
import com.codelily.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(req);

        String registrationId = req.getClientRegistration().getRegistrationId(); // google | kakao
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 표준화된 유저 정보 추출
        OAuth2UserInfo info = switch (registrationId) {
            case "google" -> extractGoogle(attributes);
            case "kakao"  -> extractKakao(attributes);
            default -> throw new OAuth2AuthenticationException(new OAuth2Error("invalid_provider"),
                    "지원하지 않는 OAuth2 제공자: " + registrationId);
        };

        // 사용자 조회/생성
        User user = userRepository.findByProviderAndProviderId(info.provider(), info.providerId())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(info.email().orElse(null))
                        .passwordHash(null)
                        .nickname(info.nickname())
                        .avatarUrl(info.avatarUrl().orElse(null))
                        .role(User.Role.USER)
                        .provider(info.provider())
                        .providerId(info.providerId())
                        .emailVerified(info.emailVerified().orElse(false))
                        .build()));

        // 권한 & OAuth2User 반환 (attributes에 우리 User id도 포함)
        Map<String, Object> merged = new HashMap<>(attributes);
        merged.put("userId", user.getId());
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                merged,
                "sub" // 기본 nameAttributeKey (google openid) – kakao는 id지만, 실제 사용은 attributes에서 userId 사용
        );
    }

    // --- Provider 별 속성 파서 ---

    private OAuth2UserInfo extractGoogle(Map<String, Object> attr) {
        String sub = (String) attr.get("sub");
        String name = (String) attr.getOrDefault("name", "GoogleUser");
        String picture = (String) attr.get("picture");
        String email = (String) attr.get("email");
        Boolean emailVerified = (Boolean) attr.getOrDefault("email_verified", false);

        return new OAuth2UserInfo("google", sub, name, Optional.ofNullable(email), Optional.ofNullable(picture), Optional.of(emailVerified));
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo extractKakao(Map<String, Object> attr) {
        String id = String.valueOf(attr.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) attr.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        String nickname = (String) profile.getOrDefault("nickname", "KakaoUser");
        String profileImage = (String) profile.get("profile_image_url");
        String email = (String) kakaoAccount.get("email");
        Boolean emailVerified = (Boolean) kakaoAccount.getOrDefault("is_email_verified", false);

        return new OAuth2UserInfo("kakao", id, nickname, Optional.ofNullable(email), Optional.ofNullable(profileImage), Optional.of(emailVerified));
    }

    // 표준화 DTO (record)
    private record OAuth2UserInfo(
            String provider,
            String providerId,
            String nickname,
            Optional<String> email,
            Optional<String> avatarUrl,
            Optional<Boolean> emailVerified
    ) {}
}
