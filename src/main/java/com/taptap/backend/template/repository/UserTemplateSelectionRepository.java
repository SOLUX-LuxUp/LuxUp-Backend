package com.taptap.backend.template.repository;

import com.taptap.backend.template.entity.UserTemplateSelection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTemplateSelectionRepository extends JpaRepository<UserTemplateSelection, Long> {
    Optional<UserTemplateSelection> findByUserId(Long userId);
}