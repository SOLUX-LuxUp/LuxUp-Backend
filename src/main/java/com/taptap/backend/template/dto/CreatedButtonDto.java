package com.taptap.backend.template.dto;

import com.taptap.backend.button.entity.Button;

public record CreatedButtonDto(
        Long buttonId,
        String buttonName,
        String iconName,
        String iconColor,
        Long categoryId,
        Long sourcePresetId
) {
    public static CreatedButtonDto from(Button button) {
        return new CreatedButtonDto(
                button.getButtonId(),
                button.getButtonName(),
                button.getIconName(),
                button.getIconColor(),
                button.getCategoryId(),
                button.getSourcePresetId()
        );
    }
}