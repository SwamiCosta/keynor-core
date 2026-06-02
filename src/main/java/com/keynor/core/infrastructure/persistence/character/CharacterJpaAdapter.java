package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.out.CharacterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CharacterJpaAdapter implements CharacterRepository {

    private final CharacterJpaRepository jpaRepository;
    private final CharacterMapper mapper;

    public CharacterJpaAdapter(CharacterJpaRepository jpaRepository, CharacterMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Character save(Character character) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(character)));
    }

    @Override
    public Optional<Character> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public PageResult<Character> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
        Specification<CharacterEntity> spec = CharacterSpecifications.fromFilter(filter);
        PageRequest springPage = PageRequest.of(pageRequest.page(), pageRequest.size());
        Page<CharacterEntity> page = jpaRepository.findAll(spec, springPage);
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }
}
