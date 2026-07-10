package com.keynor.core.infrastructure.persistence.archetype;

import com.keynor.core.domain.model.shared.Language;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "archetypes")
public class ArchetypeEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String element;

    @Column
    private String suit;

    @Column
    private String vocation;

    @Column
    private String temperament;

    @Column(name = "cognitive_function")
    private String cognitiveFunction;

    @Column(name = "self_relation")
    private String selfRelation;

    @Column(columnDefinition = "TEXT")
    private String description;

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
    public String getElement() { return element; }
    public void setElement(String element) { this.element = element; }
    public String getSuit() { return suit; }
    public void setSuit(String suit) { this.suit = suit; }
    public String getVocation() { return vocation; }
    public void setVocation(String vocation) { this.vocation = vocation; }
    public String getTemperament() { return temperament; }
    public void setTemperament(String temperament) { this.temperament = temperament; }
    public String getCognitiveFunction() { return cognitiveFunction; }
    public void setCognitiveFunction(String cognitiveFunction) { this.cognitiveFunction = cognitiveFunction; }
    public String getSelfRelation() { return selfRelation; }
    public void setSelfRelation(String selfRelation) { this.selfRelation = selfRelation; }
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
