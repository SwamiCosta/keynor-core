package com.keynor.core.infrastructure.persistence.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class TimelineEmbeddable {

    // Legacy free-text columns, kept NOT NULL by V5. Dual-written alongside the
    // *_era_id columns below until they are dropped in a separate, separately
    // authorized migration (see V9__add_timeline_era_fk_columns.sql).
    private String timelineFounded;
    private String timelineDestroyed;

    @Column(name = "timeline_founded_era_id")
    private UUID timelineFoundedEraId;

    @Column(name = "timeline_destroyed_era_id")
    private UUID timelineDestroyedEraId;

    public String getTimelineFounded() { return timelineFounded; }
    public void setTimelineFounded(String timelineFounded) { this.timelineFounded = timelineFounded; }
    public String getTimelineDestroyed() { return timelineDestroyed; }
    public void setTimelineDestroyed(String timelineDestroyed) { this.timelineDestroyed = timelineDestroyed; }
    public UUID getTimelineFoundedEraId() { return timelineFoundedEraId; }
    public void setTimelineFoundedEraId(UUID timelineFoundedEraId) { this.timelineFoundedEraId = timelineFoundedEraId; }
    public UUID getTimelineDestroyedEraId() { return timelineDestroyedEraId; }
    public void setTimelineDestroyedEraId(UUID timelineDestroyedEraId) { this.timelineDestroyedEraId = timelineDestroyedEraId; }
}
