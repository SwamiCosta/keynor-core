package com.keynor.core.infrastructure.persistence.place;

import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "places")
public class PlaceEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "universe_entity_images", joinColumns = @JoinColumn(name = "entity_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "display_order")
    private List<String> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "place_categories", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "category")
    private List<PlaceCategory> categories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private MapType mapType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(nullable = false, length = 2)
    private Language language;

    @Column(nullable = false)
    private UUID translationGroupId;

    @Embedded
    private TimelineEmbeddable timeline;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<PlaceCategory> getCategories() { return categories; }
    public void setCategories(List<PlaceCategory> categories) { this.categories = categories; }
    public MapType getMapType() { return mapType; }
    public void setMapType(MapType mapType) { this.mapType = mapType; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
    public void setTranslationGroupId(UUID translationGroupId) { this.translationGroupId = translationGroupId; }
    public TimelineEmbeddable getTimeline() { return timeline; }
    public void setTimeline(TimelineEmbeddable timeline) { this.timeline = timeline; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
