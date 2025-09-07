package com.codelily.backend.controller;

import com.codelily.backend.domain.User;
import com.codelily.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 내 정보 조회 */
    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Optional<User> user = userService.findById(userId);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** 사용자 단건 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);
        return user.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** 닉네임 중복 체크 */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(userService.existsByNickname(nickname));
    }

    /** 프로필 업데이트 */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateProfile(
            @PathVariable Long id,
            @RequestParam String nickname,
            @RequestParam(required = false) String avatarUrl) {
        return ResponseEntity.ok(userService.updateProfile(id, nickname, avatarUrl));
    }
}
