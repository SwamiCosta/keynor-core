package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CharacterJpaRepository
        extends JpaRepository<CharacterEntity, UUID>,
                JpaSpecificationExecutor<CharacterEntity> {

    boolean existsByNameAndLanguage(String name, Language language);
}
