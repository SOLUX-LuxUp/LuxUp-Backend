package com.taptap.backend.team.service;

import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.TeamButton;
import com.taptap.backend.team.entity.TeamButtonCategory;
import com.taptap.backend.team.entity.TeamButtonRecord;
import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.team.exception.TeamException;
import com.taptap.backend.team.repository.TeamButtonCategoryRepository;
import com.taptap.backend.team.repository.TeamButtonRecordRepository;
import com.taptap.backend.team.repository.TeamButtonRepository;
import com.taptap.backend.team.repository.TeamMemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TeamInsightService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamButtonRepository teamButtonRepository;
    private final TeamButtonCategoryRepository teamButtonCategoryRepository;
    private final TeamButtonRecordRepository teamButtonRecordRepository;

    public TeamInsightService(TeamMemberRepository teamMemberRepository, TeamButtonRepository teamButtonRepository,
                               TeamButtonCategoryRepository teamButtonCategoryRepository, TeamButtonRecordRepository teamButtonRecordRepository) {
        this.teamMemberRepository = teamMemberRepository;
        this.teamButtonRepository = teamButtonRepository;
        this.teamButtonCategoryRepository = teamButtonCategoryRepository;
        this.teamButtonRecordRepository = teamButtonRecordRepository;
    }

    public DailyInsightResponseDto getDailyInsight(Long userId, Long teamId, LocalDate targetDate) {
        requireMembership(teamId, userId);
        LocalDate date = targetDate == null ? LocalDate.now() : targetDate;
        Context ctx = buildContext(teamId);

        List<TeamButtonRecord> records = teamButtonRecordRepository.findAllByTeamIdAndDeletedAtIsNullAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(
                teamId, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        Aggregate agg = aggregate(records, ctx);

        List<InsightTimelineItemDto> timeline = records.stream()
                .sorted(Comparator.comparing(TeamButtonRecord::getRecordedAt).reversed())
                .map(r -> {
                    TeamButton b = ctx.buttons.get(r.getTeamButtonId());
                    return new InsightTimelineItemDto(
                            r.getTeamButtonId(), b == null ? null : b.getButtonName(),
                            b == null ? null : b.getIconName(), b == null ? null : b.getIconColor(),
                            r.getRecordedAt(), memberProfile(ctx, r.getUserId())
                    );
                })
                .collect(Collectors.toList());

        return new DailyInsightResponseDto(
                teamId, date, agg.totalTapCount, agg.topButton, timeline, agg.categories, agg.buttonTapCounts, agg.memberActivity
        );
    }

    public WeeklyInsightResponseDto getWeeklyInsight(Long userId, Long teamId, LocalDate weekStart) {
        requireMembership(teamId, userId);
        LocalDate start = weekStart == null ? LocalDate.now().with(java.time.DayOfWeek.MONDAY) : weekStart;
        LocalDate end = start.plusDays(6);
        Context ctx = buildContext(teamId);

        List<TeamButtonRecord> records = teamButtonRecordRepository.findAllByTeamIdAndDeletedAtIsNullAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(
                teamId, start.atStartOfDay(), end.plusDays(1).atStartOfDay());

        Aggregate agg = aggregate(records, ctx);

        List<WeeklyDailyTapCountDto> dailyTapCounts = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            LocalDate d = day;
            List<TeamButtonRecord> dayRecords = records.stream()
                    .filter(r -> r.getRecordedAt().toLocalDate().isEqual(d))
                    .collect(Collectors.toList());
            List<InsightCategoryTapCountDto> dayCategories = categoryBreakdown(dayRecords, ctx);
            dailyTapCounts.add(new WeeklyDailyTapCountDto(d, (long) dayRecords.size(), dayCategories));
        }

        return new WeeklyInsightResponseDto(
                teamId, start, end, agg.totalTapCount, agg.topButton, dailyTapCounts, agg.buttonTapCounts, agg.memberActivity
        );
    }

    public MonthlyInsightResponseDto getMonthlyInsight(Long userId, Long teamId, Integer year, Integer month) {
        requireMembership(teamId, userId);
        LocalDate now = LocalDate.now();
        int y = year == null ? now.getYear() : year;
        int m = month == null ? now.getMonthValue() : month;
        YearMonth yearMonth = YearMonth.of(y, m);
        Context ctx = buildContext(teamId);

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay();
        List<TeamButtonRecord> records = teamButtonRecordRepository.findAllByTeamIdAndDeletedAtIsNullAndRecordedAtGreaterThanEqualAndRecordedAtLessThan(
                teamId, start, end);

        Aggregate agg = aggregate(records, ctx);

        Map<LocalDate, Long> countsByDay = records.stream()
                .collect(Collectors.groupingBy(r -> r.getRecordedAt().toLocalDate(), Collectors.counting()));
        List<MonthlyDailyTapCountDto> dailyTapCounts = new ArrayList<>();
        for (int d = 1; d <= yearMonth.lengthOfMonth(); d++) {
            LocalDate date = yearMonth.atDay(d);
            dailyTapCounts.add(new MonthlyDailyTapCountDto(date, countsByDay.getOrDefault(date, 0L)));
        }

        long total = agg.totalTapCount == null ? 0 : agg.totalTapCount;
        List<MonthlyCategoryTapCountDto> categoryTapCounts = agg.categories.stream()
                .map(c -> new MonthlyCategoryTapCountDto(
                        c.categoryId(), c.categoryName(), c.categoryColor(), c.tapCount(),
                        total == 0 ? 0.0 : Math.round((c.tapCount() / (double) total) * 100.0) / 100.0
                ))
                .collect(Collectors.toList());

        List<MonthlyButtonTapCountDto> buttonTapCounts = agg.buttonTapCounts.stream()
                .map(b -> new MonthlyButtonTapCountDto(b.teamButtonId(), b.buttonName(), b.tapCount()))
                .collect(Collectors.toList());

        return new MonthlyInsightResponseDto(
                teamId, y, m, agg.totalTapCount, agg.topButton, dailyTapCounts, categoryTapCounts, buttonTapCounts, agg.memberActivity
        );
    }

    // ---- aggregation helpers ----

    private Aggregate aggregate(List<TeamButtonRecord> records, Context ctx) {
        long totalTapCount = records.size();

        Map<Long, Long> countsByButton = records.stream()
                .collect(Collectors.groupingBy(TeamButtonRecord::getTeamButtonId, Collectors.counting()));

        InsightTopButtonDto topButton = countsByButton.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> {
                    Long teamButtonId = entry.getKey();
                    TeamButton b = ctx.buttons.get(teamButtonId);
                    List<MemberProfileDto> tappedMembers = records.stream()
                            .filter(r -> r.getTeamButtonId().equals(teamButtonId))
                            .map(TeamButtonRecord::getUserId)
                            .distinct()
                            .map(uid -> memberProfile(ctx, uid))
                            .collect(Collectors.toList());
                    return new InsightTopButtonDto(
                            teamButtonId, b == null ? null : b.getButtonName(), b == null ? null : b.getIconName(),
                            b == null ? null : b.getIconColor(), entry.getValue(), tappedMembers
                    );
                })
                .orElse(null);

        List<InsightButtonTapCountDto> buttonTapCounts = countsByButton.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> {
                    TeamButton b = ctx.buttons.get(entry.getKey());
                    Long categoryId = b == null ? null : b.getCategoryId();
                    TeamButtonCategory category = categoryId == null ? null : ctx.categories.get(categoryId);
                    return new InsightButtonTapCountDto(
                            entry.getKey(), b == null ? null : b.getButtonName(), b == null ? null : b.getIconName(),
                            b == null ? null : b.getIconColor(), categoryId, category == null ? null : category.getCategoryName(),
                            entry.getValue()
                    );
                })
                .collect(Collectors.toList());

        List<InsightCategoryTapCountDto> categories = categoryBreakdown(records, ctx);

        Map<Long, List<TeamButtonRecord>> recordsByMember = records.stream()
                .collect(Collectors.groupingBy(TeamButtonRecord::getUserId));
        List<InsightMemberActivityDto> memberActivity = recordsByMember.entrySet().stream()
                .map(entry -> {
                    Long uid = entry.getKey();
                    List<TeamButtonRecord> memberRecords = entry.getValue();
                    Map<Long, Long> memberButtonCounts = memberRecords.stream()
                            .collect(Collectors.groupingBy(TeamButtonRecord::getTeamButtonId, Collectors.counting()));
                    InsightMemberTopButtonDto memberTopButton = memberButtonCounts.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(e -> {
                                TeamButton b = ctx.buttons.get(e.getKey());
                                return new InsightMemberTopButtonDto(
                                        e.getKey(), b == null ? null : b.getButtonName(), b == null ? null : b.getIconName(),
                                        b == null ? null : b.getIconColor(), e.getValue()
                                );
                            })
                            .orElse(null);
                    TeamMember member = ctx.members.get(uid);
                    return new InsightMemberActivityDto(
                            uid, member == null ? null : member.getDisplayName(), member == null ? null : member.getProfileImageUrl(),
                            (long) memberRecords.size(), memberTopButton
                    );
                })
                .sorted(Comparator.comparing(InsightMemberActivityDto::tapCount).reversed())
                .collect(Collectors.toList());

        return new Aggregate(totalTapCount, topButton, buttonTapCounts, categories, memberActivity);
    }

    private List<InsightCategoryTapCountDto> categoryBreakdown(List<TeamButtonRecord> records, Context ctx) {
        Map<Long, Long> countsByCategory = records.stream()
                .map(r -> ctx.buttons.get(r.getTeamButtonId()))
                .filter(b -> b != null && b.getCategoryId() != null)
                .collect(Collectors.groupingBy(TeamButton::getCategoryId, Collectors.counting()));

        return countsByCategory.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(entry -> {
                    TeamButtonCategory category = ctx.categories.get(entry.getKey());
                    return new InsightCategoryTapCountDto(
                            entry.getKey(), category == null ? null : category.getCategoryName(),
                            category == null ? null : category.getCategoryColor(), entry.getValue()
                    );
                })
                .collect(Collectors.toList());
    }

    private MemberProfileDto memberProfile(Context ctx, Long userId) {
        TeamMember member = ctx.members.get(userId);
        return member == null
                ? new MemberProfileDto(userId, null, null)
                : new MemberProfileDto(userId, member.getDisplayName(), member.getProfileImageUrl());
    }

    private Context buildContext(Long teamId) {
        Map<Long, TeamButton> buttons = teamButtonRepository.findAllByTeamId(teamId).stream()
                .collect(Collectors.toMap(TeamButton::getTeamButtonId, b -> b));
        Map<Long, TeamButtonCategory> categories = teamButtonCategoryRepository.findAllByTeamId(teamId).stream()
                .collect(Collectors.toMap(TeamButtonCategory::getCategoryId, c -> c));
        Map<Long, TeamMember> members = teamMemberRepository.findAllByTeamIdAndDeletedAtIsNullOrderByJoinedAtAsc(teamId).stream()
                .collect(Collectors.toMap(TeamMember::getUserId, m -> m));
        return new Context(buttons, categories, members);
    }

    private void requireMembership(Long teamId, Long userId) {
        if (!teamMemberRepository.existsByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다.");
        }
    }

    private record Context(Map<Long, TeamButton> buttons, Map<Long, TeamButtonCategory> categories, Map<Long, TeamMember> members) {
    }

    private record Aggregate(Long totalTapCount, InsightTopButtonDto topButton, List<InsightButtonTapCountDto> buttonTapCounts,
                              List<InsightCategoryTapCountDto> categories, List<InsightMemberActivityDto> memberActivity) {
    }
}
