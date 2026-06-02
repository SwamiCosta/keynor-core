package com.keynor.core.application.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateItemRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> tags,
        @NotNull List<String> categories,
        String timelineFoundedEra,
        String timelineDestroyedEra) {
}
