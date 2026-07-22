package com.keynor.core.domain.model.map;

import com.keynor.core.domain.model.shared.EntityType;

import java.time.Instant;
import java.util.UUID;

public class MapPin {

    private final UUID id;
    private final String mapId;
    private final EntityType entityType;
    private final UUID entityId;
    private final double normalizedX;
    private final double normalizedY;
    private final Instant createdAt;

    public MapPin(UUID id, String mapId, EntityType entityType, UUID entityId,
                  double normalizedX, double normalizedY, Instant createdAt) {
        if (normalizedX < 0 || normalizedX > 1 || normalizedY < 0 || normalizedY > 1) {
            throw new IllegalArgumentException("Normalized coordinates must be between 0 and 1");
        }
        this.id = id;
        this.mapId = mapId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.normalizedX = normalizedX;
        this.normalizedY = normalizedY;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getMapId() { return mapId; }
    public EntityType getEntityType() { return entityType; }
    public UUID getEntityId() { return entityId; }
    public double getNormalizedX() { return normalizedX; }
    public double getNormalizedY() { return normalizedY; }
    public Instant getCreatedAt() { return createdAt; }
}
