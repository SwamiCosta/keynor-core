package com.keynor.core.domain.model.event;

import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.model.shared.UniverseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Event extends UniverseEntity {

    private List<EventCategory> categories;

    public Event(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> tags,
            List<EventCategory> categories,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt) {
        super(id, name, summary, body, tags, status, timeline, createdAt, updatedAt);
        this.categories = new ArrayList<>(categories);
    }

    public void update(String name, String summary, String body, List<String> tags, List<EventCategory> categories, Timeline timeline) {
        updateBaseFields(name, summary, body, tags, timeline);
        this.categories = new ArrayList<>(categories);
    }

    public List<EventCategory> getCategories() { return List.copyOf(categories); }
}
