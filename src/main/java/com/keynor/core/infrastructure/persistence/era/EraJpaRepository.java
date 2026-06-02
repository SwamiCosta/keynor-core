package com.keynor.core.infrastructure.persistence.era;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EraJpaRepository extends JpaRepository<EraEntity, String> {
}
