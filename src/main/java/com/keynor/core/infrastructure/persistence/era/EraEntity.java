package com.keynor.core.infrastructure.persistence.era;

import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.model.shared.Language;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eras")
public class EraEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int orderIndex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EraType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private EraImportance importance;

    @Column(columnDefinition = "TEXT")
    private String description;

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
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public EraType getType() { return type; }
    public void setType(EraType type) { this.type = type; }
    public EraImportance getImportance() { return importance; }
    public void setImportance(EraImportance importance) { this.importance = importance; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
    public void setTranslationGroupId(UUID translationGroupId) { this.translationGroupId = translationGroupId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
