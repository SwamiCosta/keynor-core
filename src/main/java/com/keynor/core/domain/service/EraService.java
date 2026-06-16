package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import com.keynor.core.domain.port.out.EraRepository;

import java.util.List;
import java.util.UUID;

public class EraService implements FindAllErasUseCase, FindEraByIdUseCase {

    private final EraRepository eraRepository;

    public EraService(EraRepository eraRepository) {
        this.eraRepository = eraRepository;
    }

    @Override
    public List<Era> findAll() {
        return eraRepository.findAllOrderedByIndex();
    }

    @Override
    public Era findById(UUID id) {
        return eraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Era", id));
    }
}
