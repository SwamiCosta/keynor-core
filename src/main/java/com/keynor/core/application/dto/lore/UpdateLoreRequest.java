package com.keynor.core.application.dto.lore;

import com.keynor.core.application.dto.shared.EntityLinkRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * `hidden` and `common` must both be explicitly resent with every update to
 * preserve an already-hidden/already-common entity's state -- like every
 * other field on this record, this is full-replacement semantics, not a
 * partial patch. Omitting either (or any JSON body that doesn't include it)
 * is interpreted as {@code false} and will un-hide / un-mark the entity as
 * common.
 */
public record UpdateLoreRequest(
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
        String password,
        boolean common) {
}
