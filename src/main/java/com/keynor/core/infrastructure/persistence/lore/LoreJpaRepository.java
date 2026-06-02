package com.keynor.core.infrastructure.persistence.lore;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LoreJpaRepository
        extends JpaRepository<LoreEntity, UUID>,
                JpaSpecificationExecutor<LoreEntity> {

    boolean existsByName(String name);
}
