package com.taptap.backend.template.dto;

import java.util.List;

public record TemplateApplyRequestDto(
        List<Long> selectedPresetIds
) {
}