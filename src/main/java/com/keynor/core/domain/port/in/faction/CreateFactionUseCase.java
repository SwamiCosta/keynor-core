package com.keynor.core.domain.port.in.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface CreateFactionUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<FactionCategory> categories,
            List<UUID> members,
            Timeline timeline,
            EntityStatus status,
            Language language,
            UUID translationGroupId,
            UUID versionGroupId,
            List<EntityLinkRef> links,
            boolean hidden,
            String riddleText,
            String password) {}

    Faction create(Command command);
}
