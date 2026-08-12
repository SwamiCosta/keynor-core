package com.keynor.core.domain.model.character;

import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.model.shared.UniverseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Character extends UniverseEntity {

    private List<CharacterCategory> categories;

    public Character(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> images,
            List<CharacterCategory> categories,
            EntityStatus status,
            Timeline timeline,
            Instant createdAt,
            Instant updatedAt,
            Language language,
            UUID translationGroupId,
            UUID versionGroupId,
            boolean hidden,
            boolean common) {
        super(id, name, summary, body, images, status, timeline, createdAt, updatedAt, language, translationGroupId, versionGroupId, hidden, common);
        this.categories = new ArrayList<>(categories);
    }

    public void update(String name, String summary, String body, List<String> images, List<CharacterCategory> categories, Timeline timeline) {
        updateBaseFields(name, summary, body, images, timeline);
        this.categories = new ArrayList<>(categories);
    }

    public List<CharacterCategory> getCategories() {
        return List.copyOf(categories);
    }
}
