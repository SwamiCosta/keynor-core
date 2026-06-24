package com.keynor.core.infrastructure.persistence.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class TimelineEmbeddable {

    @Column(name = "timeline_founded_era_id")
    private UUID timelineFoundedEraId;

    @Column(name = "timeline_destroyed_era_id")
    private UUID timelineDestroyedEraId;

    public UUID getTimelineFoundedEraId() { return timelineFoundedEraId; }
    public void setTimelineFoundedEraId(UUID timelineFoundedEraId) { this.timelineFoundedEraId = timelineFoundedEraId; }
    public UUID getTimelineDestroyedEraId() { return timelineDestroyedEraId; }
    public void setTimelineDestroyedEraId(UUID timelineDestroyedEraId) { this.timelineDestroyedEraId = timelineDestroyedEraId; }
}
