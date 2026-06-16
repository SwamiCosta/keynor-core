package com.keynor.core.domain.port.in.shared;

import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.List;
import java.util.UUID;

public interface FindLinkedEntitiesUseCase {

    List<EntityLinkSummary> findLinks(EntityType sourceType, UUID sourceId);
}
