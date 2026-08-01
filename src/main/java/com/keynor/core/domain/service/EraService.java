package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.port.in.era.CreateEraUseCase;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import com.keynor.core.domain.port.in.era.UpdateEraUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EraService implements FindAllErasUseCase, FindEraByIdUseCase, CreateEraUseCase, UpdateEraUseCase {

    private final EraRepository eraRepository;
    private final EntityLinkRepository entityLinkRepository;

    public EraService(EraRepository eraRepository, EntityLinkRepository entityLinkRepository) {
        this.eraRepository = eraRepository;
        this.entityLinkRepository = entityLinkRepository;
    }

    @Override
    public List<Era> findAll(Language language) {
        return eraRepository.findAllOrderedByIndex(language);
    }

    @Override
    public Era findById(UUID id) {
        return eraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Era", id));
    }

    @Override
    public Era create(CreateEraUseCase.Command command) {
        Instant now = Instant.now();
        UUID newId = UUID.randomUUID();
        UUID translationGroupId = command.translationGroupId() != null ? command.translationGroupId() : newId;
        Era era = new Era(
                newId,
                command.name(),
                command.orderIndex(),
                command.type(),
                command.importance(),
                command.description(),
                now,
                now,
                command.language(),
                translationGroupId);
        Era saved = eraRepository.save(era);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        entityLinkRepository.replaceLinks(EntityType.ERA, saved.getId(), links);
        return saved;
    }

    @Override
    public Era update(UUID id, UpdateEraUseCase.Command command) {
        Era era = eraRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Era", id));
        era.update(command.name(), command.orderIndex(), command.type(), command.importance(), command.description());
        Era saved = eraRepository.save(era);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        entityLinkRepository.replaceLinks(EntityType.ERA, saved.getId(), links);
        return saved;
    }
}
