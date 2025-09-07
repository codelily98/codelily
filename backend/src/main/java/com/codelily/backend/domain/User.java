package com.codelily.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** 사용자 PK */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이메일 (소셜 로그인은 optional) */
    @Column(unique = true, length = 255)
    private String email;

    /** 비밀번호 해시 (소셜 로그인 사용자는 null 허용) */
    @Column(name = "password_hash")
    private String passwordHash;

    /** 닉네임 */
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    /** 프로필 이미지 URL */
    @Column(length = 512)
    private String avatarUrl;

    /** 사용자 권한 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    /** 로그인 제공자: local / kakao / google */
    @Column(nullable = false, length = 20)
    private String provider = "local";

    /** 제공자별 사용자 고유 ID */
    @Column(name = "provider_id", length = 100)
    private String providerId;

    /** 소셜 로그인 Refresh 토큰 (선택) */
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    /** 이메일 인증 여부 */
    @Column(name = "email_verified")
    private boolean emailVerified = false;

    /** 마지막 로그인 시간 */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /** 생성일 */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 수정일 */
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /** 엔티티 저장 전 자동 생성일 설정 */
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }

    /** 엔티티 수정 전 자동 갱신 */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum Role {
        USER, ADMIN
    }
}
