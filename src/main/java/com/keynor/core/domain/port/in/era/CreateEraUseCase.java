package com.keynor.core.domain.port.in.era;

import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.model.shared.Language;

import java.util.UUID;

public interface CreateEraUseCase {

    record Command(
            String name,
            int orderIndex,
            EraType type,
            EraImportance importance,
            String description,
            Language language,
            UUID translationGroupId) {}

    Era create(Command command);
}
