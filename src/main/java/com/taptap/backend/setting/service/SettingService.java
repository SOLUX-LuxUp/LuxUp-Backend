package com.taptap.backend.setting.service;

import com.taptap.backend.setting.dto.NotificationSettingResponse;
import com.taptap.backend.setting.dto.NotificationSettingUpdateRequest;
import com.taptap.backend.setting.entity.UserNotificationSetting;
import com.taptap.backend.setting.repository.UserNotificationSettingRepository;
import com.taptap.backend.user.dto.UserProfileResponse;
import com.taptap.backend.user.dto.UserProfileUpdateRequest;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    /**
     * [안전 장치] MySQL AUTO_INCREMENT 규칙에 맞춘 테스트 유저/설정 보장 로직
     */
    @Transactional
    protected UserNotificationSetting getOrCreateTestEnvironment() {
        // 1. DB에 유저가 한 명이라도 있는지 확인하고, 있으면 첫 번째 유저를 가져옵니다.
        return userNotificationSettingRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    // 2. 만약 알림 설정이 없다면, 유저부터 먼저 생성합니다. (ID는 DB가 알아서 부여함)
                    User user = userRepository.findAll().stream().findFirst()
                            .orElseGet(() -> userRepository.save(User.builder()
                                    .email("user@example.com")
                                    .username("심세희")
                                    .profileImageUrl("https://cdn.example.com/profile/1.jpg")
                                    .loginType("EMAIL")
                                    .status("ACTIVE")
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build()));

                    // 3. 생성되거나 가져온 유저를 기반으로 알림 설정을 묶어서 저장합니다.
                    return userNotificationSettingRepository.save(UserNotificationSetting.builder()
                            .user(user)
                            .masterEnabled(true)
                            .soundEnabled(true)
                            .vibrationEnabled(true)
                            .popupOverlay(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build());
                });
    }

    /**
     * 9.1 유저 프로필 조회
     */
    @Transactional
    public UserProfileResponse.Get getUserProfile(Long userId) {
        // 무조건 데이터가 존재하는 환경을 보장받음
        UserNotificationSetting setting = getOrCreateTestEnvironment();
        User user = setting.getUser();

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
        UserNotificationSetting setting = getOrCreateTestEnvironment();
        User user = setting.getUser();

        user.updateProfile(request.getUsername(), request.getProfileImageUrl());

        return UserProfileResponse.Update.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    /**
     * 9.2 알림 마스터 설정 조회
     */
    @Transactional
    public NotificationSettingResponse getNotificationSettings(Long userId) {
        UserNotificationSetting setting = getOrCreateTestEnvironment();

        return NotificationSettingResponse.builder()
                .masterEnabled(setting.getMasterEnabled())
                .soundEnabled(setting.getSoundEnabled())
                .vibrationEnabled(setting.getVibrationEnabled())
                .popupOverlay(setting.getPopupOverlay())
                .build();
    }

    /**
     * 9.2 알림 마스터 설정 수정
     */
    @Transactional
    public NotificationSettingResponse updateNotificationSettings(Long userId, NotificationSettingUpdateRequest request) {
        UserNotificationSetting setting = getOrCreateTestEnvironment();

        setting.updateSettings(
                request.getMasterEnabled(),
                request.getSoundEnabled(),
                request.getVibrationEnabled(),
                request.getPopupOverlay()
        );

        return NotificationSettingResponse.builder()
                .masterEnabled(setting.getMasterEnabled())
                .soundEnabled(setting.getSoundEnabled())
                .vibrationEnabled(setting.getVibrationEnabled())
                .popupOverlay(setting.getPopupOverlay())
                .build();
    }
}