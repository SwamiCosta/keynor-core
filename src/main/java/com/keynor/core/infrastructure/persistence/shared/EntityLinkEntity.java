package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "entity_links")
public class EntityLinkEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private EntityType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private EntityType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public EntityType getSourceType() { return sourceType; }
    public void setSourceType(EntityType sourceType) { this.sourceType = sourceType; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public EntityType getTargetType() { return targetType; }
    public void setTargetType(EntityType targetType) { this.targetType = targetType; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
