package com.keynor.core.domain.model.shared;

import java.time.Instant;
import java.util.UUID;

public class EntityLink {

    private final UUID id;
    private final EntityType sourceType;
    private final UUID sourceId;
    private final EntityType targetType;
    private final UUID targetId;
    private final Instant createdAt;

    public EntityLink(UUID id, EntityType sourceType, UUID sourceId, EntityType targetType, UUID targetId, Instant createdAt) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public EntityType getSourceType() { return sourceType; }
    public UUID getSourceId() { return sourceId; }
    public EntityType getTargetType() { return targetType; }
    public UUID getTargetId() { return targetId; }
    public Instant getCreatedAt() { return createdAt; }
}
