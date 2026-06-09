package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.infrastructure.persistence.shared.TimelineEmbeddable;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "characters")
public class CharacterEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "character_tags", joinColumns = @JoinColumn(name = "character_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "universe_entity_images", joinColumns = @JoinColumn(name = "entity_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "display_order")
    private List<String> images = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "character_categories", joinColumns = @JoinColumn(name = "character_id"))
    @Column(name = "category")
    private List<CharacterCategory> categories = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

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
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<CharacterCategory> getCategories() { return categories; }
    public void setCategories(List<CharacterCategory> categories) { this.categories = categories; }
    public EntityStatus getStatus() { return status; }
    public void setStatus(EntityStatus status) { this.status = status; }
    public TimelineEmbeddable getTimeline() { return timeline; }
    public void setTimeline(TimelineEmbeddable timeline) { this.timeline = timeline; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
