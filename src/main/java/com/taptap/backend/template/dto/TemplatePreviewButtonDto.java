package com.taptap.backend.template.dto;

import com.taptap.backend.template.entity.TemplateButton;

public record TemplatePreviewButtonDto(
        Long presetId,
        String buttonName,
        String iconName,
        String iconColor
) {
    public static TemplatePreviewButtonDto from(TemplateButton tb) {
        return new TemplatePreviewButtonDto(
                tb.getPresetId(),
                tb.getButtonName(),
                tb.getIconName(),
                tb.getIconColor()
        );
    }
}