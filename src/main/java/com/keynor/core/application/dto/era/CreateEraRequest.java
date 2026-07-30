package com.keynor.core.application.dto.era;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateEraRequest(
        @NotBlank String name,
        @NotNull Integer orderIndex,
        @NotBlank String type,
        String importance,
        String description,
        @NotBlank String language,
        UUID translationGroupId,
        List<EntityLinkRequest> links) {
}
