package com.keynor.core.infrastructure.persistence.faction;

import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.out.FactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FactionJpaAdapter implements FactionRepository {

    private final FactionJpaRepository jpaRepository;
    private final FactionMapper mapper;

    public FactionJpaAdapter(FactionJpaRepository jpaRepository, FactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Faction save(Faction faction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(faction)));
    }

    @Override
    public Optional<Faction> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) { return jpaRepository.existsById(id); }

    @Override
    public boolean existsByNameAndLanguage(String name, Language language) { return jpaRepository.existsByNameAndLanguage(name, language); }

    @Override
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }

    @Override
    public PageResult<Faction> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
        Page<FactionEntity> page = jpaRepository.findAll(
                FactionSpecifications.fromFilter(filter),
                PageRequest.of(pageRequest.page(), pageRequest.size()));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
