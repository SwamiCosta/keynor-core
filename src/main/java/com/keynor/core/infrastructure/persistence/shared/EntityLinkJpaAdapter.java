package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityLink;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class EntityLinkJpaAdapter implements EntityLinkRepository {

    private final EntityLinkJpaRepository jpaRepository;
    private final EntityLinkMapper mapper;

    public EntityLinkJpaAdapter(EntityLinkJpaRepository jpaRepository, EntityLinkMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<EntityLink> findBySource(EntityType sourceType, UUID sourceId) {
        return jpaRepository.findBySourceTypeAndSourceId(sourceType, sourceId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void replaceLinks(EntityType sourceType, UUID sourceId, List<EntityLinkRef> targets) {
        jpaRepository.deleteBySourceTypeAndSourceId(sourceType, sourceId);
        Instant now = Instant.now();
        List<EntityLinkEntity> entities = targets.stream().map(target -> {
            EntityLinkEntity entity = new EntityLinkEntity();
            entity.setId(UUID.randomUUID());
            entity.setSourceType(sourceType);
            entity.setSourceId(sourceId);
            entity.setTargetType(target.targetType());
            entity.setTargetId(target.targetId());
            entity.setCreatedAt(now);
            return entity;
        }).toList();
        jpaRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void deleteAllForEntity(EntityType type, UUID id) {
        jpaRepository.deleteBySourceTypeAndSourceIdOrTargetTypeAndTargetId(type, id, type, id);
    }
}
