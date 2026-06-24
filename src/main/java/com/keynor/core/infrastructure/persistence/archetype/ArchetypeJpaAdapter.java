package com.keynor.core.infrastructure.persistence.archetype;

import com.keynor.core.domain.model.archetype.Archetype;
import com.keynor.core.domain.port.out.ArchetypeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ArchetypeJpaAdapter implements ArchetypeRepository {

    private final ArchetypeJpaRepository jpaRepository;
    private final ArchetypeMapper mapper;

    public ArchetypeJpaAdapter(ArchetypeJpaRepository jpaRepository, ArchetypeMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Archetype> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Archetype> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
