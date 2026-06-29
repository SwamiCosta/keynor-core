package com.keynor.core.application.dto.place;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

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
        List<EntityLinkRequest> links) {
}
