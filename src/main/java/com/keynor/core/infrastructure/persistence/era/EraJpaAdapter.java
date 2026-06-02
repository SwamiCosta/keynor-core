package com.keynor.core.infrastructure.persistence.era;

import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.port.out.EraRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EraJpaAdapter implements EraRepository {

    private final EraJpaRepository jpaRepository;
    private final EraMapper mapper;

    public EraJpaAdapter(EraJpaRepository jpaRepository, EraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Era> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<Era> findById(String id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
