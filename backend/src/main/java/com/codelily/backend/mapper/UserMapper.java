package com.codelily.backend.mapper;

import com.codelily.backend.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    /**
     * 사용자 단건 조회 (id 기준)
     */
    Optional<User> findById(@Param("id") Long id);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(@Param("email") String email);

    /**
     * provider + providerId 로 사용자 조회 (소셜 로그인 전용)
     */
    Optional<User> findByProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );

    /**
     * 사용자 전체 조회
     */
    List<User> findAll();

    /**
     * 특정 기간 내 가입한 사용자 수
     */
    int countNewUsers(
            @Param("start") String startDate,
            @Param("end") String endDate
    );

    /**
     * 사용자 프로필 업데이트
     */
    int updateProfile(
            @Param("id") Long id,
            @Param("nickname") String nickname,
            @Param("avatarUrl") String avatarUrl
    );

    /**
     * 소셜 로그인 사용자 Refresh 토큰 저장
     */
    int updateRefreshToken(
            @Param("id") Long id,
            @Param("refreshToken") String refreshToken
    );
}
