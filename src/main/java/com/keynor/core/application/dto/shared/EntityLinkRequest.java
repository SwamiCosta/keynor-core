package com.keynor.core.application.dto.shared;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EntityLinkRequest(
        @NotBlank String targetType,
        @NotNull UUID targetId) {
}
