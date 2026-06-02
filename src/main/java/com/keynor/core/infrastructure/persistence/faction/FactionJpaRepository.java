package com.keynor.core.infrastructure.persistence.faction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FactionJpaRepository
        extends JpaRepository<FactionEntity, UUID>,
                JpaSpecificationExecutor<FactionEntity> {

    boolean existsByName(String name);
}
