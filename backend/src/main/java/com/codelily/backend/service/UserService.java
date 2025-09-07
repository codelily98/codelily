package com.codelily.backend.service;

import com.codelily.backend.domain.User;
import com.codelily.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** 회원가입 (Local) */
    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    /** 사용자 조회 */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /** 이메일로 사용자 조회 */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /** 닉네임 중복 체크 */
    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /** 프로필 업데이트 */
    public User updateProfile(Long id, String nickname, String avatarUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        user.setNickname(nickname);
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }
}
