package com.keynor.core.application.dto.map;

import com.keynor.core.application.dto.shared.LinkedEntityResponse;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.model.map.PinShape;
import com.keynor.core.domain.model.shared.EntityLinkSummary;

import java.time.Instant;
import java.util.UUID;

public record MapPinResponse(
        UUID id,
        String mapId,
        String name,
        LinkedEntityResponse entity,
        PinShape shape,
        double normalizedX,
        double normalizedY,
        Instant createdAt) {

    /**
     * {@code summary} is null for a pin with no linked entity. {@code name}
     * is the pin's own custom name if set, otherwise the linked entity's
     * live name -- except when the entity is still-locked hidden content, in
     * which case it stays null regardless (a black pin must never surface a
     * label, custom or derived -- see root ARCHITECTURE.md, Hidden Content &
     * Black Pins).
     */
    public static MapPinResponse from(MapPin pin, EntityLinkSummary summary) {
        LinkedEntityResponse entity = summary != null ? LinkedEntityResponse.from(summary) : null;
        boolean lockedHidden = summary != null && summary.hidden();
        String name = lockedHidden ? null : pin.getName() != null ? pin.getName() : summary != null ? summary.name() : null;
        return new MapPinResponse(
                pin.getId(),
                pin.getMapId(),
                name,
                entity,
                pin.getShape(),
                pin.getNormalizedX(),
                pin.getNormalizedY(),
                pin.getCreatedAt());
    }
}
