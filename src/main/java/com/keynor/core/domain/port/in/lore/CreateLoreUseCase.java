package com.keynor.core.domain.port.in.lore;

import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateLoreUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<LoreCategory> categories,
            Timeline timeline,
            EntityStatus status,
            List<EntityLinkRef> links) {}

    Lore create(Command command);
}
