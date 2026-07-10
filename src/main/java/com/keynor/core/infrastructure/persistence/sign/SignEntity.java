package com.keynor.core.infrastructure.persistence.sign;

import com.keynor.core.domain.model.shared.Language;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "signs")
public class SignEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "sign_order", nullable = false)
    private int signOrder;

    @Column(name = "season_time")
    private String seasonTime;

    @Column(name = "archetype_id", nullable = false)
    private UUID archetypeId;

    @Column(name = "sub_archetype")
    private String subArchetype;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private Language language;

    @Column(nullable = false)
    private UUID translationGroupId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getSignOrder() { return signOrder; }
    public void setSignOrder(int signOrder) { this.signOrder = signOrder; }
    public String getSeasonTime() { return seasonTime; }
    public void setSeasonTime(String seasonTime) { this.seasonTime = seasonTime; }
    public UUID getArchetypeId() { return archetypeId; }
    public void setArchetypeId(UUID archetypeId) { this.archetypeId = archetypeId; }
    public String getSubArchetype() { return subArchetype; }
    public void setSubArchetype(String subArchetype) { this.subArchetype = subArchetype; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
    public void setTranslationGroupId(UUID translationGroupId) { this.translationGroupId = translationGroupId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
