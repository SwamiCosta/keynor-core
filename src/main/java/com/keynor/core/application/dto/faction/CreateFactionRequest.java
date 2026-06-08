package com.keynor.core.application.dto.faction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateFactionRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> tags,
        @NotNull List<String> categories,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra) {
}
