package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.faction.*;
import com.keynor.core.domain.port.out.FactionRepository;

import java.time.Instant;
import java.util.UUID;

public class FactionService implements
        CreateFactionUseCase,
        UpdateFactionUseCase,
        ChangeFactionStatusUseCase,
        DeleteFactionUseCase,
        FindFactionByIdUseCase,
        FindAllFactionsUseCase {

    private final FactionRepository factionRepository;

    public FactionService(FactionRepository factionRepository) {
        this.factionRepository = factionRepository;
    }

    @Override
    public Faction create(CreateFactionUseCase.Command command) {
        if (factionRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Faction", command.name());
        }
        Instant now = Instant.now();
        Faction faction = new Faction(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.tags(),
                command.images(),
                command.categories(),
                EntityStatus.DRAFT,
                command.timeline(),
                now,
                now);
        return factionRepository.save(faction);
    }

    @Override
    public Faction update(UUID id, UpdateFactionUseCase.Command command) {
        Faction faction = factionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faction", id));
        faction.update(command.name(), command.summary(), command.body(), command.tags(), command.images(), command.categories(), command.timeline());
        return factionRepository.save(faction);
    }

    @Override
    public Faction changeStatus(UUID id, EntityStatus newStatus) {
        Faction faction = factionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faction", id));
        faction.changeStatus(newStatus);
        return factionRepository.save(faction);
    }

    @Override
    public void delete(UUID id) {
        if (!factionRepository.existsById(id)) {
            throw new EntityNotFoundException("Faction", id);
        }
        factionRepository.deleteById(id);
    }

    @Override
    public Faction findById(UUID id) {
        return factionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faction", id));
    }

    @Override
    public PageResult<Faction> findAll(EntityFilter filter, PageRequest pageRequest) {
        return factionRepository.findAll(filter, pageRequest);
    }
}
