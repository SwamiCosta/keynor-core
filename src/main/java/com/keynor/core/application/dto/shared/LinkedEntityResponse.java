package com.keynor.core.application.dto.shared;

import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.util.UUID;

public record LinkedEntityResponse(
        String type,
        UUID id,
        String name,
        String status,
        boolean hidden) {

    /**
     * A hidden target's name/status are never revealed here, regardless of
     * caller or unlock state -- this record backs both MapPinResponse and
     * every entity's "links" field, so redacting centrally here is what
     * keeps a black pin (or a related-entity link to hidden content)
     * from leaking the target's identity before it is unlocked. See root
     * ARCHITECTURE.md -- "Cross-Project Feature: Hidden Content & Black Pins".
     */
    public static LinkedEntityResponse from(EntityLinkSummary summary) {
        if (summary.hidden()) {
            return new LinkedEntityResponse(summary.type().name(), summary.id(), null, null, true);
        }
        return new LinkedEntityResponse(
                summary.type().name(),
                summary.id(),
                summary.name(),
                summary.status().name(),
                false);
    }
}
