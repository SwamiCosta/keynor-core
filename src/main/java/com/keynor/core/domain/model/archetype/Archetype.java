package com.keynor.core.domain.model.archetype;

import com.keynor.core.domain.model.shared.Language;

import java.time.Instant;
import java.util.UUID;

public class Archetype {

    private final UUID id;
    private final String name;
    private final String element;
    private final String suit;
    private final String vocation;
    private final String temperament;
    private final String cognitiveFunction;
    private final String selfRelation;
    private final String description;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Language language;
    private final UUID translationGroupId;

    public Archetype(
            UUID id,
            String name,
            String element,
            String suit,
            String vocation,
            String temperament,
            String cognitiveFunction,
            String selfRelation,
            String description,
            Instant createdAt,
            Instant updatedAt,
            Language language,
            UUID translationGroupId) {
        this.id = id;
        this.name = name;
        this.element = element;
        this.suit = suit;
        this.vocation = vocation;
        this.temperament = temperament;
        this.cognitiveFunction = cognitiveFunction;
        this.selfRelation = selfRelation;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.language = language;
        this.translationGroupId = translationGroupId;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getElement() { return element; }
    public String getSuit() { return suit; }
    public String getVocation() { return vocation; }
    public String getTemperament() { return temperament; }
    public String getCognitiveFunction() { return cognitiveFunction; }
    public String getSelfRelation() { return selfRelation; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Language getLanguage() { return language; }
    public UUID getTranslationGroupId() { return translationGroupId; }
}
