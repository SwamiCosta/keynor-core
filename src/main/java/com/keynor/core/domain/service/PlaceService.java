package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.place.*;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.PlaceRepository;

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

    public PlaceService(PlaceRepository placeRepository, EntityLinkRepository entityLinkRepository) {
        this.placeRepository = placeRepository;
        this.entityLinkRepository = entityLinkRepository;
    }

    @Override
    public Place create(CreatePlaceUseCase.Command command) {
        if (placeRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Place", command.name());
        }
        Instant now = Instant.now();
        Place place = new Place(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.tags(),
                command.images(),
                command.categories(),
                command.mapType(),
                EntityStatus.DRAFT,
                command.timeline(),
                now,
                now);
        Place saved = placeRepository.save(place);
        entityLinkRepository.replaceLinks(EntityType.PLACE, saved.getId(), command.links() != null ? command.links() : List.of());
        return saved;
    }

    @Override
    public Place update(UUID id, UpdatePlaceUseCase.Command command) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Place", id));
        place.update(command.name(), command.summary(), command.body(), command.tags(), command.images(), command.categories(), command.mapType(), command.timeline());
        Place saved = placeRepository.save(place);
        entityLinkRepository.replaceLinks(EntityType.PLACE, saved.getId(), command.links() != null ? command.links() : List.of());
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
}
