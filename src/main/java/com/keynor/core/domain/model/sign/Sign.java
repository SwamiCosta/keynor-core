package com.keynor.core.domain.model.sign;

import java.time.Instant;
import java.util.UUID;

public class Sign {

    private final UUID id;
    private final String name;
    private final int signOrder;
    private final String seasonTime;
    private final UUID archetypeId;
    private final String subArchetype;
    private final String summary;
    private final String body;
    private final Instant createdAt;
    private final Instant updatedAt;

    public Sign(
            UUID id,
            String name,
            int signOrder,
            String seasonTime,
            UUID archetypeId,
            String subArchetype,
            String summary,
            String body,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.signOrder = signOrder;
        this.seasonTime = seasonTime;
        this.archetypeId = archetypeId;
        this.subArchetype = subArchetype;
        this.summary = summary;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getSignOrder() { return signOrder; }
    public String getSeasonTime() { return seasonTime; }
    public UUID getArchetypeId() { return archetypeId; }
    public String getSubArchetype() { return subArchetype; }
    public String getSummary() { return summary; }
    public String getBody() { return body; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
