package com.taptap.backend.setting.service;

import com.taptap.backend.setting.dto.NotificationSettingDto;
import com.taptap.backend.setting.entity.UserNotificationSetting;
import com.taptap.backend.setting.repository.UserNotificationSettingRepository;
import com.taptap.backend.user.dto.UserProfileDto;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    /**
     * 9.1 유저 프로필 조회
     */
    public UserProfileDto.Response getUserProfile(Long userId) {
        User user = userRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 유저입니다."));

        return UserProfileDto.Response.builder()
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
    public UserProfileDto.Response updateUserProfile(Long userId, UserProfileDto.UpdateRequest request) {
        User user = userRepository.findByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 탈퇴한 유저입니다."));

        // Entity 내부 편의 메서드를 통해 상태 변경
        user.updateProfile(request.getUsername(), request.getProfileImageUrl());

        return UserProfileDto.Response.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .profileImageUrl(user.getProfileImageUrl())
                .loginType(user.getLoginType())
                .email(user.getEmail())
                .build();
    }

    /**
     * 9.2 알림 마스터 설정 조회
     */
    public NotificationSettingDto.Response getNotificationSettings(Long userId) {
        UserNotificationSetting setting = userNotificationSettingRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 알림 설정이 존재하지 않습니다."));

        return NotificationSettingDto.Response.builder()
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
    public NotificationSettingDto.Response updateNotificationSettings(Long userId, NotificationSettingDto.UpdateRequest request) {
        UserNotificationSetting setting = userNotificationSettingRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저의 알림 설정이 존재하지 않습니다."));

        // 변경 요청이 들어온 값만 선택적으로 변경 처리
        setting.updateSettings(
                request.getMasterEnabled(),
                request.getSoundEnabled(),
                request.getVibrationEnabled(),
                request.getPopupOverlay()
        );

        return NotificationSettingDto.Response.builder()
                .masterEnabled(setting.getMasterEnabled())
                .soundEnabled(setting.getSoundEnabled())
                .vibrationEnabled(setting.getVibrationEnabled())
                .popupOverlay(setting.getPopupOverlay())
                .build();
    }
}