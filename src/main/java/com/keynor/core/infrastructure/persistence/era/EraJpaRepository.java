package com.keynor.core.infrastructure.persistence.era;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EraJpaRepository extends JpaRepository<EraEntity, UUID> {

    List<EraEntity> findAllByOrderByOrderIndexAsc();

    Optional<EraEntity> findByName(String name);
}
