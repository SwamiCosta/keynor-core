package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.place.*;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.PlaceRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class PlaceService implements
        CreatePlaceUseCase,
        UpdatePlaceUseCase,
        ChangePlaceStatusUseCase,
        DeletePlaceUseCase,
        FindPlaceByIdUseCase,
        FindAllPlacesUseCase {

    private final PlaceRepository placeRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final EraRepository eraRepository;
    private final UniverseEntityLookupRepository universeEntityLookupRepository;
    private final CreateHiddenContentLockUseCase createHiddenContentLockUseCase;

    public PlaceService(
            PlaceRepository placeRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository,
            CreateHiddenContentLockUseCase createHiddenContentLockUseCase) {
        this.placeRepository = placeRepository;
        this.entityLinkRepository = entityLinkRepository;
        this.eraRepository = eraRepository;
        this.universeEntityLookupRepository = universeEntityLookupRepository;
        this.createHiddenContentLockUseCase = createHiddenContentLockUseCase;
    }

    @Override
    public Place create(CreatePlaceUseCase.Command command) {
        if (placeRepository.existsByNameAndLanguage(command.name(), command.language())) {
            throw new DuplicateEntityNameException("Place", command.name());
        }
        validateTimeline(command.timeline());
        Instant now = Instant.now();
        EntityStatus initialStatus = command.status() != null ? command.status() : EntityStatus.DRAFT;
        UUID newId = UUID.randomUUID();
        UUID translationGroupId = command.translationGroupId() != null ? command.translationGroupId() : newId;
        UUID versionGroupId = command.versionGroupId() != null ? command.versionGroupId() : newId;
        Place place = new Place(
                newId,
                command.name(),
                command.summary(),
                command.body(),
                command.images(),
                command.categories(),
                command.mapType(),
                initialStatus,
                command.timeline(),
                now,
                now,
                command.language(),
                translationGroupId,
                versionGroupId,
                command.hidden(),
                command.common());
        Place saved = placeRepository.save(place);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.PLACE, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.PLACE, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
    }

    @Override
    public Place update(UUID id, UpdatePlaceUseCase.Command command) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Place", id));
        validateTimeline(command.timeline());
        if (command.hidden() && (command.riddleText() == null || command.riddleText().isBlank()
                || command.password() == null || command.password().isBlank())) {
            throw new IllegalArgumentException("riddleText and password are required when hidden is true");
        }
        place.update(command.name(), command.summary(), command.body(), command.images(), command.categories(), command.mapType(), command.timeline());
        place.setHidden(command.hidden());
        place.setCommon(command.common());
        Place saved = placeRepository.save(place);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.PLACE, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.PLACE, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
    }

    @Override
    public Place changeStatus(UUID id, EntityStatus newStatus) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Place", id));
        place.changeStatus(newStatus);
        return placeRepository.save(place);
    }

    @Override
    public void delete(UUID id) {
        if (!placeRepository.existsById(id)) {
            throw new EntityNotFoundException("Place", id);
        }
        placeRepository.deleteById(id);
        entityLinkRepository.deleteAllForEntity(EntityType.PLACE, id);
    }

    @Override
    public Place findById(UUID id) {
        return placeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Place", id));
    }

    @Override
    public PageResult<Place> findAll(EntityFilter filter, PageRequest pageRequest) {
        return placeRepository.findAll(filter, pageRequest);
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
