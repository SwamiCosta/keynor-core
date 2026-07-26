package com.keynor.core.domain.model.event;

import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
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
            List<String> images,
            List<EventCategory> categories,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt,
            Language language,
            UUID translationGroupId,
            boolean hidden) {
        super(id, name, summary, body, images, status, timeline, createdAt, updatedAt, language, translationGroupId, hidden);
        this.categories = new ArrayList<>(categories);
    }

    public void update(String name, String summary, String body, List<String> images, List<EventCategory> categories, Timeline timeline) {
        updateBaseFields(name, summary, body, images, timeline);
        this.categories = new ArrayList<>(categories);
    }

    public List<EventCategory> getCategories() { return List.copyOf(categories); }
}
