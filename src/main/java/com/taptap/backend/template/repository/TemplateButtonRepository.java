package com.taptap.backend.template.repository;

import com.taptap.backend.template.entity.TemplateButton;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateButtonRepository extends JpaRepository<TemplateButton, Long> {
    List<TemplateButton> findByTemplateIdAndDeletedAtIsNullOrderByDisplayOrderAsc(Long templateId);
}