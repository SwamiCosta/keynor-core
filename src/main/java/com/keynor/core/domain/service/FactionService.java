package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.faction.Faction;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.faction.*;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.FactionRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class FactionService implements
        CreateFactionUseCase,
        UpdateFactionUseCase,
        ChangeFactionStatusUseCase,
        DeleteFactionUseCase,
        FindFactionByIdUseCase,
        FindAllFactionsUseCase {

    private final FactionRepository factionRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final EraRepository eraRepository;
    private final UniverseEntityLookupRepository universeEntityLookupRepository;
    private final CreateHiddenContentLockUseCase createHiddenContentLockUseCase;

    public FactionService(
            FactionRepository factionRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository,
            CreateHiddenContentLockUseCase createHiddenContentLockUseCase) {
        this.factionRepository = factionRepository;
        this.entityLinkRepository = entityLinkRepository;
        this.eraRepository = eraRepository;
        this.universeEntityLookupRepository = universeEntityLookupRepository;
        this.createHiddenContentLockUseCase = createHiddenContentLockUseCase;
    }

    @Override
    public Faction create(CreateFactionUseCase.Command command) {
        if (factionRepository.existsByNameAndLanguage(command.name(), command.language())) {
            throw new DuplicateEntityNameException("Faction", command.name());
        }
        validateTimeline(command.timeline());
        Instant now = Instant.now();
        EntityStatus initialStatus = command.status() != null ? command.status() : EntityStatus.DRAFT;
        UUID newId = UUID.randomUUID();
        UUID translationGroupId = command.translationGroupId() != null ? command.translationGroupId() : newId;
        Faction faction = new Faction(
                newId,
                command.name(),
                command.summary(),
                command.body(),
                command.images(),
                command.categories(),
                command.members(),
                initialStatus,
                command.timeline(),
                now,
                now,
                command.language(),
                translationGroupId,
                command.hidden());
        Faction saved = factionRepository.save(faction);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.FACTION, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.FACTION, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
    }

    @Override
    public Faction update(UUID id, UpdateFactionUseCase.Command command) {
        Faction faction = factionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faction", id));
        validateTimeline(command.timeline());
        faction.update(command.name(), command.summary(), command.body(), command.images(), command.categories(), command.members(), command.timeline());
        Faction saved = factionRepository.save(faction);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.FACTION, saved.getId(), links);
        return saved;
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
        entityLinkRepository.deleteAllForEntity(EntityType.FACTION, id);
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

    private void validateTimeline(Timeline timeline) {
        if (timeline == null) return;
        validateEraName(timeline.founded());
        validateEraName(timeline.destroyed());
    }

    private void validateEraName(String eraName) {
        if (eraName == null) return;
        if (eraRepository.findByName(eraName).isEmpty()) {
            throw new UnknownEraNameException(eraName);
        }
    }
}
