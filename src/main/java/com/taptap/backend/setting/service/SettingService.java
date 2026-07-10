package com.taptap.backend.setting.service;

import com.taptap.backend.config.exception.CustomException;
import com.taptap.backend.config.exception.ErrorCode;
import com.taptap.backend.setting.dto.NotificationSettingResponse;
import com.taptap.backend.setting.dto.NotificationSettingUpdateRequest;
import com.taptap.backend.setting.entity.UserNotificationSetting;
import com.taptap.backend.setting.repository.UserNotificationSettingRepository;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 9.2 알림 마스터 설정 도메인 서비스.
 * (9.1 프로필 로직은 user.service.UserProfileService로 분리했습니다.)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    /**
     * 9.2 알림 마스터 설정 조회
     */
    public NotificationSettingResponse getNotificationSettings(Long userId) {
        UserNotificationSetting setting = getOrCreateSetting(userId);
        return toResponse(setting);
    }

    /**
     * 9.2 알림 마스터 설정 수정 (수정할 필드만 포함 가능)
     */
    @Transactional
    public NotificationSettingResponse updateNotificationSettings(Long userId, NotificationSettingUpdateRequest request) {
        UserNotificationSetting setting = getOrCreateSetting(userId);

        setting.updateSettings(
                request.getMasterEnabled(),
                request.getSoundEnabled(),
                request.getVibrationEnabled(),
                request.getPopupOverlay()
        );

        return toResponse(setting);
    }

    /**
     * userId 기준으로 알림 설정을 조회하고, 없으면 기본값으로 새로 생성한다.
     * (회원가입 시점에 항상 함께 생성되는 게 이상적이지만, 그 로직은 인증 파트 소관이라
     *  여기서도 방어적으로 생성해준다.)
     *
     * ⚠️ 기존 getOrCreateTestEnvironment()는 파라미터 userId를 전혀 쓰지 않고
     *    userNotificationSettingRepository.findAll().stream().findFirst()로
     *    "DB에서 가장 먼저 만들어진 유저"의 설정을 반환하는 버그가 있었습니다.
     *    → 항상 findByUser_UserId(userId)로 특정 유저를 지정해서 조회해야 합니다.
     *    (findByUser_UserId는 이미 Repository에 만들어져 있었는데 안 쓰이고 있었어요.)
     */
    private UserNotificationSetting getOrCreateSetting(Long userId) {
        return userNotificationSettingRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findByUserIdAndStatus(userId, "ACTIVE")
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                    UserNotificationSetting newSetting = UserNotificationSetting.builder()
                            .user(user)
                            .masterEnabled(true)
                            .soundEnabled(true)
                            .vibrationEnabled(true)
                            .popupOverlay(false)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();

                    return userNotificationSettingRepository.save(newSetting);
                });
    }

    private NotificationSettingResponse toResponse(UserNotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .masterEnabled(setting.getMasterEnabled())
                .soundEnabled(setting.getSoundEnabled())
                .vibrationEnabled(setting.getVibrationEnabled())
                .popupOverlay(setting.getPopupOverlay())
                .build();
    }
}