package com.keynor.core.application.dto.archetype;

import com.keynor.core.domain.model.archetype.Archetype;

import java.util.UUID;

public record ArchetypeResponse(
        UUID id,
        String name,
        String element,
        String suit,
        String vocation,
        String temperament,
        String cognitiveFunction,
        String selfRelation,
        String description,
        String language,
        UUID translationGroupId) {

    public static ArchetypeResponse from(Archetype archetype) {
        return new ArchetypeResponse(
                archetype.getId(),
                archetype.getName(),
                archetype.getElement(),
                archetype.getSuit(),
                archetype.getVocation(),
                archetype.getTemperament(),
                archetype.getCognitiveFunction(),
                archetype.getSelfRelation(),
                archetype.getDescription(),
                archetype.getLanguage().name(),
                archetype.getTranslationGroupId());
    }
}
