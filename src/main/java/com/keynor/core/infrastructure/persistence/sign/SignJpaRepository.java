package com.keynor.core.infrastructure.persistence.sign;

import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SignJpaRepository extends JpaRepository<SignEntity, UUID> {

    List<SignEntity> findAllByLanguageOrderBySignOrderAsc(Language language);
}
