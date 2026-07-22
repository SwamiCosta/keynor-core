package com.keynor.core.application.dto.map;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.UUID;

public record MapPinResponse(
        UUID id,
        String mapId,
        LinkedEntityResponse entity,
        double normalizedX,
        double normalizedY,
        Instant createdAt) {

    public static MapPinResponse from(MapPin pin, EntityLinkSummary summary) {
        return new MapPinResponse(
                pin.getId(),
                pin.getMapId(),
                LinkedEntityResponse.from(summary),
                pin.getNormalizedX(),
                pin.getNormalizedY(),
                pin.getCreatedAt());
    }
}
