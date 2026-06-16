package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.shared.EntityLink;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityType;

import java.util.List;
import java.util.UUID;

public interface EntityLinkRepository {

    List<EntityLink> findBySource(EntityType sourceType, UUID sourceId);

    void replaceLinks(EntityType sourceType, UUID sourceId, List<EntityLinkRef> targets);

    void deleteAllForEntity(EntityType type, UUID id);
}
