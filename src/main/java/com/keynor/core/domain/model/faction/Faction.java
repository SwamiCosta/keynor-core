package com.keynor.core.domain.model.faction;

import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.model.shared.UniverseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Faction extends UniverseEntity {

    private List<FactionCategory> categories;
    private List<UUID> members;

    public Faction(
            UUID id,
            String name,
            String summary,
            String body,
            List<String> images,
            List<FactionCategory> categories,
            List<UUID> members,
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
        this.members = new ArrayList<>(members);
    }

    public void update(String name, String summary, String body, List<String> images, List<FactionCategory> categories, List<UUID> members, Timeline timeline) {
        updateBaseFields(name, summary, body, images, timeline);
        this.categories = new ArrayList<>(categories);
        this.members = new ArrayList<>(members);
    }

    public List<FactionCategory> getCategories() { return List.copyOf(categories); }
    public List<UUID> getMembers() { return List.copyOf(members); }
}
