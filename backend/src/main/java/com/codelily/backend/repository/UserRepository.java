package com.codelily.backend.repository;

import com.codelily.backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자 검색
    Optional<User> findByEmail(String email);

    // 닉네임으로 사용자 검색
    Optional<User> findByNickname(String nickname);
    
    
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    // 중복 이메일 체크
    boolean existsByEmail(String email);

    // 중복 닉네임 체크
    boolean existsByNickname(String nickname);
}
