package com.keynor.core.domain.service;

import com.keynor.core.domain.model.shared.EntityLink;
import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.in.shared.FindEntitySummaryUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityLinkService implements FindLinkedEntitiesUseCase, FindEntitySummaryUseCase {

    private final EntityLinkRepository entityLinkRepository;
    private final UniverseEntityLookupRepository universeEntityLookupRepository;

    public EntityLinkService(EntityLinkRepository entityLinkRepository, UniverseEntityLookupRepository universeEntityLookupRepository) {
        this.entityLinkRepository = entityLinkRepository;
        this.universeEntityLookupRepository = universeEntityLookupRepository;
    }

    @Override
    public List<EntityLinkSummary> findLinks(EntityType sourceType, UUID sourceId) {
        List<EntityLink> links = entityLinkRepository.findBySource(sourceType, sourceId);
        return links.stream()
                .map(link -> universeEntityLookupRepository.findSummary(link.getTargetType(), link.getTargetId()))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    @Override
    public Optional<EntityLinkSummary> findSummary(EntityType type, UUID id) {
        return universeEntityLookupRepository.findSummary(type, id);
    }
}
