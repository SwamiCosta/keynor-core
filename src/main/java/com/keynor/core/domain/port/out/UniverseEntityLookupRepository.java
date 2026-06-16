package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.Optional;
import java.util.UUID;

public interface UniverseEntityLookupRepository {

    Optional<EntityLinkSummary> findSummary(EntityType type, UUID id);
}
