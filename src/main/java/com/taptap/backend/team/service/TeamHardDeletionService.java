package com.taptap.backend.team.service;

import com.taptap.backend.team.entity.Team;
import com.taptap.backend.team.entity.TeamButton;
import com.taptap.backend.team.repository.TeamButtonCategoryRepository;
import com.taptap.backend.team.repository.TeamButtonRecordRepository;
import com.taptap.backend.team.repository.TeamButtonRepository;
import com.taptap.backend.team.repository.TeamButtonUserSettingRepository;
import com.taptap.backend.team.repository.TeamMemberButtonSharingRepository;
import com.taptap.backend.team.repository.TeamMemberRepository;
import com.taptap.backend.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 팀 삭제 요청 후 3일 유예기간이 지난 팀을 실제로 하드 삭제하는 배치 로직.
 * (팀 삭제 API는 deletedAt만 세팅하는 soft delete이며, 실제 데이터 정리는 여기서 수행)
 */
@Service
public class TeamHardDeletionService {

    private static final int GRACE_PERIOD_DAYS = 3;

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamButtonRepository teamButtonRepository;
    private final TeamButtonCategoryRepository teamButtonCategoryRepository;
    private final TeamButtonRecordRepository teamButtonRecordRepository;
    private final TeamButtonUserSettingRepository teamButtonUserSettingRepository;
    private final TeamMemberButtonSharingRepository teamMemberButtonSharingRepository;

    public TeamHardDeletionService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
                                    TeamButtonRepository teamButtonRepository, TeamButtonCategoryRepository teamButtonCategoryRepository,
                                    TeamButtonRecordRepository teamButtonRecordRepository, TeamButtonUserSettingRepository teamButtonUserSettingRepository,
                                    TeamMemberButtonSharingRepository teamMemberButtonSharingRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamButtonRepository = teamButtonRepository;
        this.teamButtonCategoryRepository = teamButtonCategoryRepository;
        this.teamButtonRecordRepository = teamButtonRecordRepository;
        this.teamButtonUserSettingRepository = teamButtonUserSettingRepository;
        this.teamMemberButtonSharingRepository = teamMemberButtonSharingRepository;
    }

    @Transactional
    public void hardDeleteExpiredTeams() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(GRACE_PERIOD_DAYS);
        List<Team> expiredTeams = teamRepository.findAllByDeletedAtIsNotNullAndDeletedAtBefore(threshold);
        for (Team team : expiredTeams) {
            hardDelete(team);
        }
    }

    private void hardDelete(Team team) {
        Long teamId = team.getTeamId();

        // TeamButtonUserSetting은 teamId가 아닌 teamButtonId 기준이므로 팀 버튼 목록을 먼저 조회
        List<Long> teamButtonIds = teamButtonRepository.findAllByTeamId(teamId).stream()
                .map(TeamButton::getTeamButtonId)
                .toList();
        for (Long teamButtonId : teamButtonIds) {
            teamButtonUserSettingRepository.deleteAllByTeamButtonId(teamButtonId);
        }

        teamButtonRecordRepository.deleteAllByTeamId(teamId);
        teamButtonRepository.deleteAllByTeamId(teamId);
        teamButtonCategoryRepository.deleteAllByTeamId(teamId);
        teamMemberButtonSharingRepository.deleteAllByTeamId(teamId);
        teamMemberRepository.deleteAllByTeamId(teamId);
        teamRepository.delete(team);
    }
}
