package com.keynor.core.infrastructure.persistence.archetype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArchetypeJpaRepository extends JpaRepository<ArchetypeEntity, UUID> {
}
