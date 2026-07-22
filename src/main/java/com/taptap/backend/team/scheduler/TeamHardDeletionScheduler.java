package com.taptap.backend.team.scheduler;

import com.taptap.backend.team.service.TeamHardDeletionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TeamHardDeletionScheduler {

    private final TeamHardDeletionService teamHardDeletionService;

    public TeamHardDeletionScheduler(TeamHardDeletionService teamHardDeletionService) {
        this.teamHardDeletionService = teamHardDeletionService;
    }

    // 매 정시마다 실행 - deletedAt + 3일이 지난 팀을 찾아 하드 삭제
    @Scheduled(cron = "0 0 * * * *")
    public void hardDeleteExpiredTeams() {
        teamHardDeletionService.hardDeleteExpiredTeams();
    }
}
