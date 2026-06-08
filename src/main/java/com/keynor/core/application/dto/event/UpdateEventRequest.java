package com.keynor.core.application.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateEventRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> tags,
        List<String> images,
        @NotNull List<String> categories,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra) {
}
