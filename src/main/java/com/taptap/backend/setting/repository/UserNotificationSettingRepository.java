package com.taptap.backend.setting.repository;

import com.taptap.backend.setting.entity.UserNotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
    // user_id 외래키를 기반으로 설정 조회
    Optional<UserNotificationSetting> findByUser_UserId(Long userId);
}