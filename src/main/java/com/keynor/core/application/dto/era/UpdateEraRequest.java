package com.keynor.core.application.dto.era;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateEraRequest(
        @NotBlank String name,
        @NotNull Integer orderIndex,
        @NotBlank String type,
        String importance,
        String description,
        List<EntityLinkRequest> links) {
}
