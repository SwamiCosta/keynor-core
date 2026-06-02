package com.keynor.core.infrastructure.persistence.lore;

import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.out.LoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class LoreJpaAdapter implements LoreRepository {

    private final LoreJpaRepository jpaRepository;
    private final LoreMapper mapper;

    public LoreJpaAdapter(LoreJpaRepository jpaRepository, LoreMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Lore save(Lore lore) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(lore)));
    }

    @Override
    public Optional<Lore> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) { return jpaRepository.existsById(id); }

    @Override
    public boolean existsByName(String name) { return jpaRepository.existsByName(name); }

    @Override
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }

    @Override
    public PageResult<Lore> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
        Page<LoreEntity> page = jpaRepository.findAll(
                LoreSpecifications.fromFilter(filter),
                PageRequest.of(pageRequest.page(), pageRequest.size()));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
