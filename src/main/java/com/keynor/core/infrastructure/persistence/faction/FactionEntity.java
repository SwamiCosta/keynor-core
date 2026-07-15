package com.keynor.core.infrastructure.persistence.faction;

import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "factions")
public class FactionEntity {

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
    @CollectionTable(name = "faction_categories", joinColumns = @JoinColumn(name = "faction_id"))
    @Column(name = "category")
    private List<FactionCategory> categories = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "faction_members", joinColumns = @JoinColumn(name = "faction_id"))
    @Column(name = "character_id")
    @OrderColumn(name = "display_order")
    private List<UUID> members = new ArrayList<>();

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
    public List<FactionCategory> getCategories() { return categories; }
    public void setCategories(List<FactionCategory> categories) { this.categories = categories; }
    public List<UUID> getMembers() { return members; }
    public void setMembers(List<UUID> members) { this.members = members; }
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
