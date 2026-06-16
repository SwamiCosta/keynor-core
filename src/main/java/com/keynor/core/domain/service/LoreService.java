package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.lore.*;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.LoreRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class LoreService implements
        CreateLoreUseCase,
        UpdateLoreUseCase,
        ChangeLoreStatusUseCase,
        DeleteLoreUseCase,
        FindLoreByIdUseCase,
        FindAllLoreUseCase {

    private final LoreRepository loreRepository;
    private final EntityLinkRepository entityLinkRepository;

    public LoreService(LoreRepository loreRepository, EntityLinkRepository entityLinkRepository) {
        this.loreRepository = loreRepository;
        this.entityLinkRepository = entityLinkRepository;
    }

    @Override
    public Lore create(CreateLoreUseCase.Command command) {
        if (loreRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Lore", command.name());
        }
        Instant now = Instant.now();
        EntityStatus initialStatus = command.status() != null ? command.status() : EntityStatus.DRAFT;
        Lore lore = new Lore(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.tags(),
                command.images(),
                command.categories(),
                initialStatus,
                command.timeline(),
                now,
                now);
        Lore saved = loreRepository.save(lore);
        entityLinkRepository.replaceLinks(EntityType.LORE, saved.getId(), command.links() != null ? command.links() : List.of());
        return saved;
    }

    @Override
    public Lore update(UUID id, UpdateLoreUseCase.Command command) {
        Lore lore = loreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lore", id));
        lore.update(command.name(), command.summary(), command.body(), command.tags(), command.images(), command.categories(), command.timeline());
        Lore saved = loreRepository.save(lore);
        entityLinkRepository.replaceLinks(EntityType.LORE, saved.getId(), command.links() != null ? command.links() : List.of());
        return saved;
    }

    @Override
    public Lore changeStatus(UUID id, EntityStatus newStatus) {
        Lore lore = loreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lore", id));
        lore.changeStatus(newStatus);
        return loreRepository.save(lore);
    }

    @Override
    public void delete(UUID id) {
        if (!loreRepository.existsById(id)) {
            throw new EntityNotFoundException("Lore", id);
        }
        loreRepository.deleteById(id);
        entityLinkRepository.deleteAllForEntity(EntityType.LORE, id);
    }

    @Override
    public Lore findById(UUID id) {
        return loreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lore", id));
    }

    @Override
    public PageResult<Lore> findAll(EntityFilter filter, PageRequest pageRequest) {
        return loreRepository.findAll(filter, pageRequest);
    }
}
