package com.keynor.core.application.dto.event;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateEventRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> images,
        @NotNull List<String> categories,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra,
        String status,
        @NotBlank String language,
        UUID translationGroupId,
        UUID versionGroupId,
        List<EntityLinkRequest> links,
        boolean hidden,
        String riddleText,
        String password) {
}
