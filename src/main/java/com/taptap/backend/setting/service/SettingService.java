package com.taptap.backend.setting.service;

import com.taptap.backend.config.AuthException;
import com.taptap.backend.setting.dto.NotificationSettingResponse;
import com.taptap.backend.setting.dto.NotificationSettingUpdateRequest;
import com.taptap.backend.setting.entity.UserNotificationSetting;
import com.taptap.backend.setting.repository.UserNotificationSettingRepository;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 9.2 알림 마스터 설정 도메인 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettingService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final UserRepository userRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;

    public NotificationSettingResponse getNotificationSettings(Long userId) {
        UserNotificationSetting setting = getOrCreateSetting(userId);
        return toResponse(setting);
    }

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

    private UserNotificationSetting getOrCreateSetting(Long userId) {
        return userNotificationSettingRepository.findByUser_UserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .filter(u -> ACTIVE_STATUS.equals(u.getStatus()))
                            .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."));

                    UserNotificationSetting newSetting = UserNotificationSetting.builder()
                            .user(user)
                            .masterEnabled(true)
                            .soundEnabled(true)
                            .vibrationEnabled(true)
                            .popupOverlay(false)
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