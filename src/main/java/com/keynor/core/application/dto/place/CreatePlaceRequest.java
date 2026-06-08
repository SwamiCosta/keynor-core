package com.keynor.core.application.dto.place;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePlaceRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> tags,
        @NotNull List<String> categories,
        String mapType,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra) {
}
