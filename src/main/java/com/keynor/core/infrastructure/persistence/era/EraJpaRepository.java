package com.keynor.core.infrastructure.persistence.era;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EraJpaRepository extends JpaRepository<EraEntity, UUID> {

    List<EraEntity> findAllByOrderByOrderIndexAsc();
}
