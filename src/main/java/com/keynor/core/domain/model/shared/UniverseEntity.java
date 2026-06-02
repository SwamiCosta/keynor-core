package com.keynor.core.domain.model.shared;

import com.keynor.core.domain.exception.InvalidStatusTransitionException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class UniverseEntity {

    private final UUID id;
    private String name;
    private String summary;
    private String body;
    private List<String> tags;
    private EntityStatus status;
    private Timeline timeline;
    private final Instant createdAt;
    private Instant updatedAt;

    protected UniverseEntity(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> tags,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.summary = summary;
        this.body = body;
        this.tags = new ArrayList<>(tags);
        this.status = status;
        this.timeline = timeline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void changeStatus(EntityStatus newStatus) {
        if (!isValidTransition(this.status, newStatus)) {
            throw new InvalidStatusTransitionException(this.status, newStatus);
        }
        this.status = newStatus;
        this.updatedAt = Instant.now();
    }

    protected void updateBaseFields(String name, String summary, String body, List<String> tags, Timeline timeline) {
        this.name = name;
        this.summary = summary;
        this.body = body;
        this.tags = new ArrayList<>(tags);
        this.timeline = timeline;
        this.updatedAt = Instant.now();
    }

    private boolean isValidTransition(EntityStatus from, EntityStatus to) {
        return switch (from) {
            case DRAFT -> to == EntityStatus.CANON || to == EntityStatus.DEPRECATED;
            case CANON -> to == EntityStatus.DRAFT || to == EntityStatus.DEPRECATED;
            case DEPRECATED -> to == EntityStatus.DRAFT;
        };
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getSummary() { return summary; }
    public String getBody() { return body; }
    public List<String> getTags() { return List.copyOf(tags); }
    public EntityStatus getStatus() { return status; }
    public Timeline getTimeline() { return timeline; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
