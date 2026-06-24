package com.keynor.core.infrastructure.persistence.era;

import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.port.out.EraRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EraJpaAdapter implements EraRepository {

    private final EraJpaRepository jpaRepository;
    private final EraMapper mapper;

    public EraJpaAdapter(EraJpaRepository jpaRepository, EraMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Era> findAllOrderedByIndex() {
        return jpaRepository.findAllByOrderByOrderIndexAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Era> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Era> findByName(String name) {
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public Era save(Era era) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(era)));
    }
}
