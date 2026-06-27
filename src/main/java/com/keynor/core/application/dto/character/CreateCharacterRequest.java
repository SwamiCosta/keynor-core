package com.keynor.core.application.dto.character;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateCharacterRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> images,
        @NotNull List<String> categories,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra,
        List<EntityLinkRequest> links) {
}
