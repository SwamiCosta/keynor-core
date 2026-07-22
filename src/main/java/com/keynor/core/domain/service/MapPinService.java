package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.map.MapPin;
import com.keynor.core.domain.port.in.map.CreateMapPinUseCase;
import com.keynor.core.domain.port.in.map.DeleteMapPinUseCase;
import com.keynor.core.domain.port.in.map.FindMapPinsUseCase;
import com.keynor.core.domain.port.in.map.UpdateMapPinUseCase;
import com.keynor.core.domain.port.out.MapPinRepository;
import com.keynor.core.domain.port.out.MapRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class MapPinService implements CreateMapPinUseCase, UpdateMapPinUseCase, DeleteMapPinUseCase, FindMapPinsUseCase {

    private final MapPinRepository mapPinRepository;
    private final MapRepository mapRepository;
    private final UniverseEntityLookupRepository universeEntityLookupRepository;

    public MapPinService(
            MapPinRepository mapPinRepository,
            MapRepository mapRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository) {
        this.mapPinRepository = mapPinRepository;
        this.mapRepository = mapRepository;
        this.universeEntityLookupRepository = universeEntityLookupRepository;
    }

    @Override
    public MapPin create(CreateMapPinUseCase.Command command) {
        mapRepository.findById(command.mapId())
                .orElseThrow(() -> new EntityNotFoundException("GameMap", command.mapId()));

        universeEntityLookupRepository.findSummary(command.entityType(), command.entityId())
                .orElseThrow(() -> new EntityNotFoundException(command.entityType().name(), command.entityId()));

        if (mapPinRepository.existsByMapIdAndEntityTypeAndEntityId(command.mapId(), command.entityType(), command.entityId())) {
            throw new DuplicateEntityNameException("MapPin",
                    command.entityType() + ":" + command.entityId() + " on map " + command.mapId());
        }

        MapPin pin = new MapPin(
                UUID.randomUUID(),
                command.mapId(),
                command.entityType(),
                command.entityId(),
                command.normalizedX(),
                command.normalizedY(),
                Instant.now());
        return mapPinRepository.save(pin);
    }

    @Override
    public MapPin update(String mapId, UUID pinId, UpdateMapPinUseCase.Command command) {
        MapPin pin = mapPinRepository.findById(pinId)
                .orElseThrow(() -> new EntityNotFoundException("MapPin", pinId));
        if (!pin.getMapId().equals(mapId)) {
            throw new EntityNotFoundException("MapPin", pinId);
        }
        MapPin repositioned = new MapPin(
                pin.getId(), pin.getMapId(), pin.getEntityType(), pin.getEntityId(),
                command.normalizedX(), command.normalizedY(), pin.getCreatedAt());
        return mapPinRepository.save(repositioned);
    }

    @Override
    public void delete(String mapId, UUID pinId) {
        MapPin pin = mapPinRepository.findById(pinId)
                .orElseThrow(() -> new EntityNotFoundException("MapPin", pinId));
        if (!pin.getMapId().equals(mapId)) {
            throw new EntityNotFoundException("MapPin", pinId);
        }
        mapPinRepository.deleteById(pinId);
    }

    @Override
    public List<MapPin> findByMapId(String mapId) {
        mapRepository.findById(mapId)
                .orElseThrow(() -> new EntityNotFoundException("GameMap", mapId));
        return mapPinRepository.findByMapId(mapId);
    }
}
