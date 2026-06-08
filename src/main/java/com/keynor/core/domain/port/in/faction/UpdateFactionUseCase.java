package com.keynor.core.domain.port.in.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface UpdateFactionUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> tags,
            List<String> images,
            List<FactionCategory> categories,
            Timeline timeline) {}

    Faction update(UUID id, Command command);
}
