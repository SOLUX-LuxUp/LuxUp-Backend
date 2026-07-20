package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.TeamButton;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamButtonRepository extends JpaRepository<TeamButton, Long> {
    Optional<TeamButton> findByTeamButtonIdAndTeamIdAndDeletedAtIsNull(Long teamButtonId, Long teamId);
    List<TeamButton> findAllByTeamIdAndIsActiveTrueAndDeletedAtIsNull(Long teamId);

    // 팀 인사이트 - 삭제된 버튼도 과거 기록의 이름 표시를 위해 포함
    List<TeamButton> findAllByTeamId(Long teamId);
}
