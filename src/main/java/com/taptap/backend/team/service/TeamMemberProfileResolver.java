package com.taptap.backend.team.service;

import com.taptap.backend.team.entity.TeamMember;
import com.taptap.backend.user.entity.User;
import com.taptap.backend.user.repository.UserRepository;
import org.springframework.stereotype.Component;

// 팀원 프로필 사진 결정 로직 (기획 확정 A안, 김누리님 - 2026-08-04)
// "팀 내 프로필 수정"으로 직접 커스터마이징한 적이 없으면 개인 파트의 최신 프로필 사진을 실시간으로 반영하고,
// 커스터마이징한 적이 있으면 팀원 정보에 저장된 값을 그대로 유지한다.
@Component
public class TeamMemberProfileResolver {

    private final UserRepository userRepository;

    public TeamMemberProfileResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String resolveProfileImageUrl(TeamMember member) {
        if (member == null) {
            return null;
        }
        if (Boolean.TRUE.equals(member.getProfileImageCustomized())) {
            return member.getProfileImageUrl();
        }
        return userRepository.findById(member.getUserId())
                .map(User::getProfileImageUrl)
                .orElse(member.getProfileImageUrl());
    }
}
