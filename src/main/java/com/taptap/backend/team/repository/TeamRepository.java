package com.taptap.backend.team.repository;

import com.taptap.backend.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByInviteCode(String inviteCode);
    Optional<Team> findByInviteCodeAndDeletedAtIsNull(String inviteCode);
    Optional<Team> findByTeamIdAndDeletedAtIsNull(Long teamId);

    // 팀 기본 이름 생성 - 사용자가 생성한 활성 팀 이름과의 충돌 확인용
    List<Team> findAllByCreatedByAndDeletedAtIsNull(Long createdBy);

    // 팀 하드 삭제 배치 - deletedAt(삭제 요청 시각) + 3일이 지난, 아직 하드 삭제되지 않은 팀 조회
    List<Team> findAllByDeletedAtIsNotNullAndDeletedAtBefore(LocalDateTime threshold);
}
