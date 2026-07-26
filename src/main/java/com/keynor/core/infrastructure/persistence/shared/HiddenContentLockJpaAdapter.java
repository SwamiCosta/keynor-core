package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.HiddenContentLock;
import com.keynor.core.domain.port.out.HiddenContentLockRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class HiddenContentLockJpaAdapter implements HiddenContentLockRepository {

    private final HiddenContentLockJpaRepository jpaRepository;

    public HiddenContentLockJpaAdapter(HiddenContentLockJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<HiddenContentLock> findByEntity(EntityType type, UUID id) {
        return jpaRepository.findById(new HiddenContentLockId(type.name(), id)).map(this::toDomain);
    }

    @Override
    public HiddenContentLock save(HiddenContentLock lock) {
        HiddenContentLockEntity entity = new HiddenContentLockEntity();
        entity.setId(new HiddenContentLockId(lock.entityType().name(), lock.entityId()));
        entity.setRiddleText(lock.riddleText());
        entity.setPasswordHash(lock.passwordHash());
        entity.setCreatedAt(lock.createdAt());
        entity.setUpdatedAt(lock.updatedAt());
        return toDomain(jpaRepository.save(entity));
    }

    private HiddenContentLock toDomain(HiddenContentLockEntity entity) {
        return new HiddenContentLock(
                EntityType.valueOf(entity.getId().getEntityType()),
                entity.getId().getEntityId(),
                entity.getRiddleText(),
                entity.getPasswordHash(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
