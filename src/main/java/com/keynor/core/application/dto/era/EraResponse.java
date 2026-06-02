package com.keynor.core.application.dto.era;

import com.keynor.core.domain.model.era.Era;

public record EraResponse(
        String id,
        String name,
        int eraOrder,
        String period,
        String summary,
        String mapType,
        String defaultMap,
        String color) {

    public static EraResponse from(Era era) {
        return new EraResponse(
                era.getId(),
                era.getName(),
                era.getEraOrder(),
                era.getPeriod(),
                era.getSummary(),
                era.getMapType().name(),
                era.getDefaultMap(),
                era.getColor());
    }
}
