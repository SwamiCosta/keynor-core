package com.keynor.core.application.dto.era;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEraRequest(
        @NotBlank String name,
        @NotNull Integer orderIndex,
        @NotBlank String type,
        String importance,
        String description,
        @NotBlank String language,
        UUID translationGroupId) {
}
