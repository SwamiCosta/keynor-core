package com.keynor.core.infrastructure.persistence.sign;

import com.keynor.core.domain.model.sign.Sign;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.port.out.SignRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SignJpaAdapter implements SignRepository {

    private final SignJpaRepository jpaRepository;
    private final SignMapper mapper;

    public SignJpaAdapter(SignJpaRepository jpaRepository, SignMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Sign> findAllOrderedBySignOrder(Language language) {
        return jpaRepository.findAllByLanguageOrderBySignOrderAsc(language)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Sign> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
