package com.keynor.core.domain.port.in.archetype;

import com.keynor.core.domain.model.archetype.Archetype;

import java.util.UUID;

public interface FindArchetypeByIdUseCase {
    Archetype findById(UUID id);
}
