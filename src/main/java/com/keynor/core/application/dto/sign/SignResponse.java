package com.keynor.core.application.dto.sign;

import com.keynor.core.domain.model.sign.Sign;

import java.util.UUID;

public record SignResponse(
        UUID id,
        String name,
        int signOrder,
        String seasonTime,
        UUID archetypeId,
        String subArchetype,
        String summary,
        String body) {

    public static SignResponse from(Sign sign) {
        return new SignResponse(
                sign.getId(),
                sign.getName(),
                sign.getSignOrder(),
                sign.getSeasonTime(),
                sign.getArchetypeId(),
                sign.getSubArchetype(),
                sign.getSummary(),
                sign.getBody());
    }
}
