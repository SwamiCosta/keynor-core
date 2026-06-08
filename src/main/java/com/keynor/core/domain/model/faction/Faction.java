package com.keynor.core.domain.model.faction;

import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.model.shared.UniverseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Faction extends UniverseEntity {

    private List<FactionCategory> categories;

    public Faction(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> tags,
            List<String> images,
            List<FactionCategory> categories,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt) {
        super(id, name, summary, body, tags, images, status, timeline, createdAt, updatedAt);
        this.categories = new ArrayList<>(categories);
    }

    public void update(String name, String summary, String body, List<String> tags, List<String> images, List<FactionCategory> categories, Timeline timeline) {
        updateBaseFields(name, summary, body, tags, images, timeline);
        this.categories = new ArrayList<>(categories);
    }

    public List<FactionCategory> getCategories() { return List.copyOf(categories); }
}
