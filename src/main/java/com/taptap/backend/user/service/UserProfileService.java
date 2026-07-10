package com.taptap.backend.user.service;

import com.taptap.backend.config.exception.CustomException;
import com.taptap.backend.config.exception.ErrorCode;
import com.taptap.backend.user.dto.UserProfileResponse;
import com.taptap.backend.user.dto.UserProfileUpdateRequest;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 9.1 유저 프로필 도메인 서비스.
 * (기존에는 setting.service.SettingService 안에 같이 들어있었는데,
 *  도메인 경계를 명확히 하려고 user 패키지로 분리했습니다.)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;

    /**
     * 9.1 유저 프로필 조회
     */
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

    /**
     * 9.1 유저 프로필 수정
     */
    @Transactional
    public UserProfileResponse.Update updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findActiveUser(userId);

        user.updateProfile(request.getUsername(), request.getProfileImageUrl());

        return UserProfileResponse.Update.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    private User findActiveUser(Long userId) {
        return userRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}