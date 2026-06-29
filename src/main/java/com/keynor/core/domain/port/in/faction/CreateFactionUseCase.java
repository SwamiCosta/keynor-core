package com.keynor.core.domain.port.in.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateFactionUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<FactionCategory> categories,
            Timeline timeline,
            EntityStatus status,
            List<EntityLinkRef> links) {}

    Faction create(Command command);
}
