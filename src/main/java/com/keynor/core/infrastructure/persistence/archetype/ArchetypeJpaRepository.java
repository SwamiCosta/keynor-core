package com.keynor.core.infrastructure.persistence.archetype;

import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ArchetypeJpaRepository extends JpaRepository<ArchetypeEntity, UUID> {

    List<ArchetypeEntity> findAllByLanguage(Language language);
}
