package com.keynor.core.application.dto.character;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * `hidden` must be explicitly resent with every update to preserve an
 * already-hidden entity's state -- full-replacement semantics like every
 * other field on this record, not a partial patch. Omitting it is
 * interpreted as {@code false} and will un-hide the entity.
 */
public record UpdateCharacterRequest(
        @NotBlank String name,
        String summary,
        String body,
        List<String> images,
        @NotNull List<String> categories,
        @NotBlank String timelineFoundedEra,
        String timelineDestroyedEra,
        List<EntityLinkRequest> links,
        boolean hidden,
        String riddleText,
        String password) {
}
