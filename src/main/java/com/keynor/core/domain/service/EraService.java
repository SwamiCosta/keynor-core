package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.port.in.era.CreateEraUseCase;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import com.keynor.core.domain.port.out.EraRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EraService implements FindAllErasUseCase, FindEraByIdUseCase, CreateEraUseCase {

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

    @Override
    public Era create(Command command) {
        Instant now = Instant.now();
        Era era = new Era(
                UUID.randomUUID(),
                command.name(),
                command.orderIndex(),
                command.type(),
                command.importance(),
                command.description(),
                now,
                now);
        return eraRepository.save(era);
    }
}
