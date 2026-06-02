package com.keynor.core.application.dto.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateCharacterRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> tags,
        @NotNull List<String> categories,
        String timelineFoundedEra,
        String timelineDestroyedEra) {
}
