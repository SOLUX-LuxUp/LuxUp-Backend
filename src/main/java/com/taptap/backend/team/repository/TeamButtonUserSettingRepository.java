package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamButtonUserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamButtonUserSettingRepository extends JpaRepository<TeamButtonUserSetting, Long> {
    Optional<TeamButtonUserSetting> findByTeamButtonIdAndUserId(Long teamButtonId, Long userId);
    List<TeamButtonUserSetting> findAllByTeamButtonId(Long teamButtonId);
    List<TeamButtonUserSetting> findAllByTeamButtonIdAndPermissionStatus(Long teamButtonId, String permissionStatus);

    @Modifying
    @Query("DELETE FROM TeamButtonUserSetting s WHERE s.teamButtonId = :teamButtonId")
    void deleteAllByTeamButtonId(@Param("teamButtonId") Long teamButtonId);
}
