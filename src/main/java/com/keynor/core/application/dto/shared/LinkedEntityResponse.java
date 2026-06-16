package com.keynor.core.application.dto.shared;

import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.util.UUID;

public record LinkedEntityResponse(
        String type,
        UUID id,
        String name,
        String status) {

    public static LinkedEntityResponse from(EntityLinkSummary summary) {
        return new LinkedEntityResponse(
                summary.type().name(),
                summary.id(),
                summary.name(),
                summary.status().name());
    }
}
