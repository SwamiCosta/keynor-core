package com.keynor.core.domain.port.in.lore;

import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface UpdateLoreUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<LoreCategory> categories,
            Timeline timeline,
            List<EntityLinkRef> links) {}

    Lore update(UUID id, Command command);
}
