package com.keynor.core.infrastructure.persistence.item;

import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ItemJpaRepository
        extends JpaRepository<ItemEntity, UUID>,
                JpaSpecificationExecutor<ItemEntity> {

    boolean existsByNameAndLanguage(String name, Language language);
}
