package com.keynor.core.domain.port.in.archetype;

import com.keynor.core.domain.model.archetype.Archetype;
import com.keynor.core.domain.model.shared.Language;

import java.util.List;

public interface FindAllArchetypesUseCase {
    List<Archetype> findAll(Language language);
}
