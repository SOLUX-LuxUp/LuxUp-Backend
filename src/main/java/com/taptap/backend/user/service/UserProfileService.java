package com.taptap.backend.user.service;

import com.taptap.backend.config.AuthException;
import com.taptap.backend.user.dto.UserProfileResponse;
import com.taptap.backend.user.dto.UserProfileUpdateRequest;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 9.1 유저 프로필 도메인 서비스.
 * AuthService(로그인/탈퇴)와 같은 user 패키지에 있지만, 책임을 분리해서 별도 클래스로 둠.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserRepository userRepository;

    public UserProfileResponse.Get getUserProfile(Long userId) {
        User user = findActiveUser(userId);

        return UserProfileResponse.Get.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .loginType(user.getLoginType())
                .email(user.getEmail())
                .build();
    }

    @Transactional
    public UserProfileResponse.Update updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findActiveUser(userId);

        // User 엔티티엔 편의 메서드가 없어서, null(=PATCH에서 생략된 필드)이면
        // 기존 값을 유지하도록 여기서 직접 체크한다. (updatedAt은 @PreUpdate가 자동 처리)
        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        return UserProfileResponse.Update.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
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