package com.keynor.core.application.dto.era;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEraRequest(
        @NotBlank String name,
        @NotNull Integer orderIndex,
        @NotBlank String type,
        String importance,
        String description) {
}
