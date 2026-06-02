package com.keynor.core.infrastructure.persistence.shared;

import jakarta.persistence.Embeddable;

@Embeddable
public class TimelineEmbeddable {

    private String timelineFounded;
    private String timelineDestroyed;

    public String getTimelineFounded() { return timelineFounded; }
    public void setTimelineFounded(String timelineFounded) { this.timelineFounded = timelineFounded; }
    public String getTimelineDestroyed() { return timelineDestroyed; }
    public void setTimelineDestroyed(String timelineDestroyed) { this.timelineDestroyed = timelineDestroyed; }
}
