package com.keynor.core.domain.model.place;

import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.model.shared.UniverseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Place extends UniverseEntity {

    private List<PlaceCategory> categories;
    private MapType mapType;

    public Place(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> tags,
            List<String> images,
            List<PlaceCategory> categories,
            MapType mapType,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt) {
        super(id, name, summary, body, tags, images, status, timeline, createdAt, updatedAt);
        this.categories = new ArrayList<>(categories);
        this.mapType = mapType;
    }

    public void update(String name, String summary, String body, List<String> tags, List<String> images, List<PlaceCategory> categories, MapType mapType, Timeline timeline) {
        updateBaseFields(name, summary, body, tags, images, timeline);
        this.categories = new ArrayList<>(categories);
        this.mapType = mapType;
    }

    public List<PlaceCategory> getCategories() { return List.copyOf(categories); }
    public MapType getMapType() { return mapType; }
}
