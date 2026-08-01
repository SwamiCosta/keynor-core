package com.keynor.core.domain.port.in.era;

import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.model.shared.EntityLinkRef;

import java.util.List;
import java.util.UUID;

public interface UpdateEraUseCase {

    record Command(
            String name,
            int orderIndex,
            EraType type,
            EraImportance importance,
            String description,
            List<EntityLinkRef> links) {}

    Era update(UUID id, Command command);
}
