package com.keynor.core.infrastructure.persistence.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface EventJpaRepository
        extends JpaRepository<EventEntity, UUID>,
                JpaSpecificationExecutor<EventEntity> {

    boolean existsByName(String name);
}
