package com.keynor.core.domain.model.era;

import com.keynor.core.domain.model.shared.Language;

import java.time.Instant;
import java.util.UUID;

public class Era {

    private final UUID id;
    private String name;
    private int orderIndex;
    private EraType type;
    private EraImportance importance;
    private String description;
    private final Instant createdAt;
    private Instant updatedAt;
    private final Language language;
    private final UUID translationGroupId;

    public Era(
            UUID id,
            String name,
            int orderIndex,
            EraType type,
            EraImportance importance,
            String description,
            Instant createdAt,
            Instant updatedAt,
            Language language,
            UUID translationGroupId) {
        validateTypeImportanceInvariant(type, importance);
        this.id = id;
        this.name = name;
        this.orderIndex = orderIndex;
        this.type = type;
        this.importance = importance;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.translationGroupId = translationGroupId;
    }

    public void update(String name, int orderIndex, EraType type, EraImportance importance, String description) {
        validateTypeImportanceInvariant(type, importance);
        this.name = name;
        this.orderIndex = orderIndex;
        this.type = type;
        this.importance = importance;
        this.description = description;
        this.updatedAt = Instant.now();
    }

    private static void validateTypeImportanceInvariant(EraType type, EraImportance importance) {
        if (type == EraType.POINT && importance == null) {
            throw new IllegalArgumentException(
                    "Era of type POINT must have an importance value (STANDARD or MAJOR)");
        }
        if (type == EraType.ERA && importance != null) {
            throw new IllegalArgumentException(
                    "Era of type ERA must not have an importance value — only POINT entries carry importance");
        }
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getOrderIndex() { return orderIndex; }
    public EraType getType() { return type; }
    public EraImportance getImportance() { return importance; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Language getLanguage() { return language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
}
