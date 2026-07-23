package com.taptap.backend.user.service;

import com.taptap.backend.config.AuthException;
import com.taptap.backend.user.dto.PasswordChangeRequestDto;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 기능명세서엔 없던, 프론트 요청으로 추가된 기능.
 * 비밀번호 해시/검증은 은서님 도메인(User, PasswordEncoder Bean)을 그대로 재사용한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPasswordService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequestDto request) {
        User user = findActiveUser(userId);

        // 구글 로그인 유저는 passwordHash 자체가 null이라 비밀번호 변경 대상이 아님
        if (user.getPasswordHash() == null) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "비밀번호 로그인을 사용하지 않는 계정입니다.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다.");
        }

        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "새 비밀번호가 일치하지 않습니다.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));

        if (!ACTIVE_STATUS.equals(user.getStatus())) {
            throw new AuthException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.");
        }
        return user;
    }
}