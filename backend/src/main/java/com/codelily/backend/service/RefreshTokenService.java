package com.codelily.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redis;

    private String rtKey(Long userId) { return "rt:" + userId; }
    private String blKey(String tokenIdOrHash) { return "bl:" + tokenIdOrHash; }

    /** Refresh 저장(회전 시 갱신) */
    public void storeRefresh(Long userId, String refreshToken, Duration ttl) {
        redis.opsForValue().set(rtKey(userId), refreshToken, ttl);
    }

    /** 현재 유효한 Refresh 조회 */
    public String getRefresh(Long userId) {
        return redis.opsForValue().get(rtKey(userId));
    }

    /** 로그아웃 등으로 Refresh 무효화 */
    public void revokeRefresh(Long userId) {
        redis.delete(rtKey(userId));
    }

    /** 블랙리스트 등록 (jti 또는 토큰 해시) */
    public void blacklist(String tokenIdOrHash, Duration ttl) {
        redis.opsForValue().set(blKey(tokenIdOrHash), "1", ttl);
    }

    /** 블랙리스트 확인 */
    public boolean isBlacklisted(String tokenIdOrHash) {
        return Boolean.TRUE.equals(redis.hasKey(blKey(tokenIdOrHash)));
    }
}
