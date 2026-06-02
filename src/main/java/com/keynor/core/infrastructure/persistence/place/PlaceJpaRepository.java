package com.keynor.core.infrastructure.persistence.place;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PlaceJpaRepository
        extends JpaRepository<PlaceEntity, UUID>,
                JpaSpecificationExecutor<PlaceEntity> {

    boolean existsByName(String name);
}
