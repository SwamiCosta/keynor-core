package com.keynor.core.domain.model.map;

import com.keynor.core.domain.model.shared.EntityType;

import java.time.Instant;
import java.util.UUID;

public class MapPin {

    private final UUID id;
    private final String mapId;
    private final EntityType entityType;
    private final UUID entityId;
    private final String name;
    private final UUID eraId;
    private final double normalizedX;
    private final double normalizedY;
    private final Instant createdAt;

    public MapPin(UUID id, String mapId, EntityType entityType, UUID entityId, String name, UUID eraId,
                  double normalizedX, double normalizedY, Instant createdAt) {
        if (normalizedX < 0 || normalizedX > 1 || normalizedY < 0 || normalizedY > 1) {
            throw new IllegalArgumentException("Normalized coordinates must be between 0 and 1");
        }
        if ((entityType == null) != (entityId == null)) {
            throw new IllegalArgumentException("entityType and entityId must both be present or both be absent");
        }
        boolean hasName = name != null && !name.isBlank();
        if (entityType == null && !hasName) {
            throw new IllegalArgumentException("name is required when the pin has no linked entity");
        }
        this.id = id;
        this.mapId = mapId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.name = hasName ? name : null;
        this.eraId = eraId;
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getMapId() { return mapId; }
    public EntityType getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public String getName() { return name; }
    // Null means the pin is shown regardless of the selected era (default).
    // A non-null value scopes the pin to that one era only -- see
    // MapPinService's validation against the map's own availability list.
    public UUID getEraId() { return eraId; }
    public double getNormalizedX() { return normalizedX; }
    public double getNormalizedY() { return normalizedY; }
    public Instant getCreatedAt() { return createdAt; }
}
