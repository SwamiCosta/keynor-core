package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.archetype.Archetype;
import com.keynor.core.domain.port.in.archetype.FindAllArchetypesUseCase;
import com.keynor.core.domain.port.in.archetype.FindArchetypeByIdUseCase;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.port.out.ArchetypeRepository;

import java.util.List;
import java.util.UUID;

public class ArchetypeService implements FindAllArchetypesUseCase, FindArchetypeByIdUseCase {

    private final ArchetypeRepository archetypeRepository;

    public ArchetypeService(ArchetypeRepository archetypeRepository) {
        this.archetypeRepository = archetypeRepository;
    }

    @Override
    public List<Archetype> findAll(Language language) {
        return archetypeRepository.findAllByLanguage(language);
    }

    @Override
    public Archetype findById(UUID id) {
        return archetypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Archetype", id));
    }
}
