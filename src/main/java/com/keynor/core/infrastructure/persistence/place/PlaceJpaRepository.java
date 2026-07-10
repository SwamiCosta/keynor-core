package com.keynor.core.infrastructure.persistence.place;

import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PlaceJpaRepository
        extends JpaRepository<PlaceEntity, UUID>,
                JpaSpecificationExecutor<PlaceEntity> {

    boolean existsByNameAndLanguage(String name, Language language);
}
