package com.keynor.core.application.dto.place;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreatePlaceRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> images,
        @NotNull List<String> categories,
        String mapType,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra,
        String status,
        @NotBlank String language,
        UUID translationGroupId,
        List<EntityLinkRequest> links) {
}
