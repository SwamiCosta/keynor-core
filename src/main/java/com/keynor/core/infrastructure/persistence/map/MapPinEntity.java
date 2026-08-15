package com.keynor.core.infrastructure.persistence.map;

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
@Table(name = "map_pins")
public class MapPinEntity {

    @Id
    private UUID id;

    @Column(name = "map_id", nullable = false)
    private String mapId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type")
    private EntityType entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "name")
    private String name;

    @Column(name = "normalized_x", nullable = false)
    private double normalizedX;

    @Column(name = "normalized_y", nullable = false)
    private double normalizedY;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMapId() { return mapId; }
    public void setMapId(String mapId) { this.mapId = mapId; }
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    public UUID getEntityId() { return entityId; }
    public void setEntityId(UUID entityId) { this.entityId = entityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getNormalizedX() { return normalizedX; }
    public void setNormalizedX(double normalizedX) { this.normalizedX = normalizedX; }
    public double getNormalizedY() { return normalizedY; }
    public void setNormalizedY(double normalizedY) { this.normalizedY = normalizedY; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
