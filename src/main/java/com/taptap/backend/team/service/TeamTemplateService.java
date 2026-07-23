package com.taptap.backend.team.service;

import com.taptap.backend.team.dto.*;
import com.taptap.backend.team.entity.Team;
import com.taptap.backend.team.entity.TeamButton;
import com.taptap.backend.team.entity.TeamButtonCategory;
import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.team.exception.TeamException;
import com.taptap.backend.team.repository.TeamButtonCategoryRepository;
import com.taptap.backend.team.repository.TeamButtonRepository;
import com.taptap.backend.team.repository.TeamMemberRepository;
import com.taptap.backend.team.repository.TeamRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

// 팀 템플릿 API — 최종 확정 데이터 기준 (Zen 팀장님 전달: 탭탭_팀_템플릿아이콘.xlsx, 2026-07-21 새벽 확정)
// 흐름 확정 사항:
//  1) 템플릿 선택(함께하기/협력하기)은 팀당 1회만 가능 — 선택 즉시 해당 템플릿의 카테고리만 프리셋 생성 (버튼은 생성하지 않음)
//  2) 버튼은 "추천 리스트"에서 사용자가 하나씩 선택 → 기존 "팀 공유 버튼 생성" API(POST /api/teams/{team_id}/buttons)를 그대로 재사용
//  3) 추천 리스트는 이미 생성된 버튼과 이름이 같으면 자동 제외되며, 상시 재호출 가능 (초기 화면 + "+" 상시사용 진입 모두 동일 API 사용)
//  4) categoryName이 없는("No Category") 프리셋 버튼은 카테고리를 생성하지 않고 categoryId=null로 내려감 (디자인 확인: 카테고리 배지 없이 노출)
@Service
public class TeamTemplateService {

    private record PresetButton(String categoryName, String buttonName, String iconName, String iconColor) {
    }

    private static final List<TeamTemplateDto> TEMPLATES = List.of(
            new TeamTemplateDto(1L, "together", "함께하기", "일상의 순간들을 공유해요", "가족/친구/연인"),
            new TeamTemplateDto(2L, "collaborate", "협력하기", "함께 나아가는 시간을 공유해요", "프로젝트/목표 달성")
    );

    private static List<PresetButton> presetsOf(String categoryName, Object[]... rows) {
        return List.of(rows).stream()
                .map(r -> new PresetButton(categoryName, (String) r[0], (String) r[1], (String) r[2]))
                .collect(Collectors.toList());
    }

    @SafeVarargs
    private static List<PresetButton> mergePresets(List<PresetButton>... groups) {
        return List.of(groups).stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private static final Map<Long, List<PresetButton>> PRESET_BUTTONS = Map.of(
            1L, mergePresets(
                    presetsOf("가족",
                            new Object[]{"가족 식사", "food", "yellow"},
                            new Object[]{"가족 회의", "chat", "darkgrey"},
                            new Object[]{"가족 여행", "travel", "cyan"},
                            new Object[]{"가족 사진", "camera", "indigo"},
                            new Object[]{"기념일", "celebrate", "red"},
                            new Object[]{"장보기", "shopping2", "red"},
                            new Object[]{"집안일", "clean", "green"},
                            new Object[]{"반려동물 산책", "dog", "orange"}
                    ),
                    presetsOf("친구",
                            new Object[]{"보고 싶어", "lightning", "cyan"},
                            new Object[]{"심심해", "sleep", "purple"},
                            new Object[]{"뭐해?", "chat", "black"},
                            new Object[]{"밥 먹자", "food", "orange"},
                            new Object[]{"카페 가자", "cup", "yellow"},
                            new Object[]{"놀러 가자", "travel", "cyan"},
                            new Object[]{"한잔할까?", "drink", "blue"},
                            new Object[]{"정산하자", "pay", "indigo"}
                    ),
                    presetsOf("연인",
                            new Object[]{"데이트", "fire", "pink"},
                            new Object[]{"기념일", "celebrate", "red"},
                            new Object[]{"추억", "camera", "darkgrey"},
                            new Object[]{"여행", "travel", "grey"}
                    ),
                    presetsOf(null,
                            new Object[]{"보고 싶어", "lightning", "cyan"},
                            new Object[]{"♥", "flower", "pink"}
                    )
            ),
            2L, mergePresets(
                    presetsOf("프로젝트",
                            new Object[]{"공지 등록", "book", "red"},
                            new Object[]{"회의", "chat", "green"},
                            new Object[]{"새로운 아이디어", "lightbulb", "yellow"},
                            new Object[]{"기획 업데이트", "note", "cyan"},
                            new Object[]{"개발 업데이트", "labtop", "blue"},
                            new Object[]{"디자인 업데이트", "pencil", "purple"},
                            new Object[]{"작업물 수정", "pencil", "green"},
                            new Object[]{"QA", "chat", "blue"},
                            new Object[]{"유저 테스트", "person", "red"},
                            new Object[]{"피드백", "chat", "indigo"},
                            new Object[]{"발표", "person", "black"},
                            new Object[]{"버전 업데이트", "lightning", "yellow"},
                            new Object[]{"완료", "celebrate", "orange"}
                    ),
                    presetsOf("목표 달성",
                            new Object[]{"목표 시작", "plant", "green"},
                            new Object[]{"출석", "person", "grey"},
                            new Object[]{"활동 인증", "sport2", "blue"},
                            new Object[]{"진행 공유", "music2", "purple"},
                            new Object[]{"하루 시작", "sun", "red"},
                            new Object[]{"하루 끝", "sleep", "indigo"},
                            new Object[]{"목표 달성", "celebrate", "yellow"},
                            new Object[]{"쉬어가기", "cup", "darkgrey"},
                            new Object[]{"응원", "fire", "orange"}
                    )
            )
    );

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamButtonRepository teamButtonRepository;
    private final TeamButtonCategoryRepository teamButtonCategoryRepository;

    public TeamTemplateService(TeamRepository teamRepository, TeamMemberRepository teamMemberRepository,
                                TeamButtonRepository teamButtonRepository, TeamButtonCategoryRepository teamButtonCategoryRepository) {
        this.teamRepository = teamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.teamButtonRepository = teamButtonRepository;
        this.teamButtonCategoryRepository = teamButtonCategoryRepository;
    }

    public List<TeamTemplateDto> listTemplates() {
        return TEMPLATES;
    }

    public TeamTemplateStatusResponseDto getTemplateStatus(Long userId, Long teamId) {
        requireMembership(teamId, userId);
        Team team = requireTeam(teamId);

        if (team.getTemplateId() == null) {
            return new TeamTemplateStatusResponseDto(false, null, null, null, Boolean.TRUE.equals(team.getTemplateSkipped()));
        }
        TeamTemplateDto template = findTemplate(team.getTemplateId());
        return new TeamTemplateStatusResponseDto(true, template.templateId(), template.templateType(), template.templateName(), false);
    }

    @Transactional
    public SkipTeamTemplateResponseDto skipTemplate(Long userId, Long teamId) {
        Team team = requireTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);

        if (!requester.isOwner()) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀장 권한이 없습니다.");
        }
        if (team.getTemplateId() != null) {
            throw new TeamException(HttpStatus.CONFLICT, "이미 템플릿을 선택한 팀입니다. 템플릿은 한 번만 선택할 수 있습니다.");
        }

        team.setTemplateSkipped(true);
        teamRepository.save(team);

        return new SkipTeamTemplateResponseDto(teamId, true);
    }

    @Transactional
    public ApplyTeamTemplateResponseDto selectTemplate(Long userId, Long teamId, ApplyTeamTemplateRequestDto request) {
        Team team = requireTeam(teamId);
        TeamMember requester = requireMembership(teamId, userId);

        if (!requester.isOwner()) {
            throw new TeamException(HttpStatus.FORBIDDEN, "팀장 권한이 없습니다.");
        }
        if (team.getTemplateId() != null) {
            throw new TeamException(HttpStatus.CONFLICT, "이미 템플릿을 선택한 팀입니다. 템플릿은 한 번만 선택할 수 있습니다.");
        }

        Long templateId = request == null ? null : request.templateId();
        TeamTemplateDto template = findTemplate(templateId);
        List<PresetButton> presets = PRESET_BUTTONS.getOrDefault(templateId, List.of());

        // categoryName이 있는 프리셋만 카테고리 생성 (No Category 버킷은 categoryId=null 유지)
        Map<String, Long> categoryIdByName = new LinkedHashMap<>();
        int order = 0;
        for (PresetButton preset : presets) {
            if (preset.categoryName() == null || categoryIdByName.containsKey(preset.categoryName())) {
                continue;
            }
            TeamButtonCategory saved = teamButtonCategoryRepository.save(
                    TeamButtonCategory.builder()
                            .teamId(teamId)
                            .categoryName(preset.categoryName())
                            .categoryColor(preset.iconColor()) // 카테고리 대표 색상: 해당 카테고리 첫 버튼의 색상 사용 (디자인 확정 전 임시 규칙)
                            .displayOrder(order++)
                            .build()
            );
            categoryIdByName.put(preset.categoryName(), saved.getCategoryId());
        }

        team.setTemplateId(templateId);
        teamRepository.save(team);

        List<TeamButtonCategoryResponseDto> categories = teamButtonCategoryRepository
                .findAllByTeamIdAndDeletedAtIsNullOrderByDisplayOrderAsc(teamId).stream()
                .map(c -> new TeamButtonCategoryResponseDto(c.getCategoryId(), c.getCategoryName(), c.getCategoryColor(), c.getDisplayOrder()))
                .collect(Collectors.toList());

        return new ApplyTeamTemplateResponseDto(teamId, templateId, template.templateType(), template.templateName(), categories);
    }

    public List<TemplateSuggestionDto> getSuggestions(Long userId, Long teamId) {
        requireMembership(teamId, userId);
        Team team = requireTeam(teamId);

        if (team.getTemplateId() == null) {
            return List.of();
        }

        List<PresetButton> presets = PRESET_BUTTONS.getOrDefault(team.getTemplateId(), List.of());

        Set<String> existingButtonNames = teamButtonRepository.findAllByTeamIdAndIsActiveTrueAndDeletedAtIsNull(teamId).stream()
                .map(TeamButton::getButtonName)
                .collect(Collectors.toSet());

        Map<String, Long> categoryIdByName = teamButtonCategoryRepository
                .findAllByTeamIdAndDeletedAtIsNullOrderByDisplayOrderAsc(teamId).stream()
                .collect(Collectors.toMap(TeamButtonCategory::getCategoryName, TeamButtonCategory::getCategoryId, (a, b) -> a));

        return presets.stream()
                .filter(p -> !existingButtonNames.contains(p.buttonName()))
                .map(p -> new TemplateSuggestionDto(
                        p.buttonName(), p.iconName(), p.iconColor(),
                        p.categoryName() == null ? null : categoryIdByName.get(p.categoryName()),
                        p.categoryName()
                ))
                .collect(Collectors.toList());
    }

    // ---- helpers ----

    private TeamTemplateDto findTemplate(Long templateId) {
        return TEMPLATES.stream()
                .filter(t -> t.templateId().equals(templateId))
                .findFirst()
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 템플릿입니다."));
    }

    private TeamMember requireMembership(Long teamId, Long userId) {
        return teamMemberRepository.findByTeamIdAndUserIdAndDeletedAtIsNull(teamId, userId)
                .orElseThrow(() -> new TeamException(HttpStatus.FORBIDDEN, "팀 미가입 유저입니다."));
    }

    private Team requireTeam(Long teamId) {
        return teamRepository.findByTeamIdAndDeletedAtIsNull(teamId)
                .orElseThrow(() -> new TeamException(HttpStatus.NOT_FOUND, "존재하지 않는 팀입니다."));
    }
}
