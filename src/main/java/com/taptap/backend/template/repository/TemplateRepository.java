package com.taptap.backend.template.repository;

import com.taptap.backend.template.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByIsActiveTrueAndDeletedAtIsNullOrderByTemplateIdAsc();
}