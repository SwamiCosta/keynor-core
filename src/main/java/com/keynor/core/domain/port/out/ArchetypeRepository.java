package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.archetype.Archetype;
import com.keynor.core.domain.model.shared.Language;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ArchetypeRepository {
    List<Archetype> findAllByLanguage(Language language);
    Optional<Archetype> findById(UUID id);
}
