package com.keynor.core.infrastructure.persistence.item;

import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "items")
public class ItemEntity {

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
    @CollectionTable(name = "item_categories", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "category")
    private List<ItemCategory> categories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(nullable = false, length = 2)
    private Language language;

    @Column(nullable = false)
    private UUID translationGroupId;

    @Column(nullable = false)
    private UUID versionGroupId;

    @Column(nullable = false)
    private boolean hidden;

    @Column(nullable = false)
    private boolean common;

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
    public List<ItemCategory> getCategories() { return categories; }
    public void setCategories(List<ItemCategory> categories) { this.categories = categories; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
    public void setTranslationGroupId(UUID translationGroupId) { this.translationGroupId = translationGroupId; }
    public UUID getVersionGroupId() { return versionGroupId; }
    public void setVersionGroupId(UUID versionGroupId) { this.versionGroupId = versionGroupId; }
    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }
    public boolean isCommon() { return common; }
    public void setCommon(boolean common) { this.common = common; }
    public TimelineEmbeddable getTimeline() { return timeline; }
    public void setTimeline(TimelineEmbeddable timeline) { this.timeline = timeline; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
