package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.EntityLinkSummary;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;
import com.keynor.core.infrastructure.persistence.character.CharacterJpaRepository;
import com.keynor.core.infrastructure.persistence.event.EventJpaRepository;
import com.keynor.core.infrastructure.persistence.faction.FactionJpaRepository;
import com.keynor.core.infrastructure.persistence.item.ItemJpaRepository;
import com.keynor.core.infrastructure.persistence.lore.LoreJpaRepository;
import com.keynor.core.infrastructure.persistence.place.PlaceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UniverseEntityLookupJpaAdapter implements UniverseEntityLookupRepository {

    private final CharacterJpaRepository characterJpaRepository;
    private final PlaceJpaRepository placeJpaRepository;
    private final FactionJpaRepository factionJpaRepository;
    private final ItemJpaRepository itemJpaRepository;
    private final EventJpaRepository eventJpaRepository;
    private final LoreJpaRepository loreJpaRepository;

    public UniverseEntityLookupJpaAdapter(
            CharacterJpaRepository characterJpaRepository,
            PlaceJpaRepository placeJpaRepository,
            FactionJpaRepository factionJpaRepository,
            ItemJpaRepository itemJpaRepository,
            EventJpaRepository eventJpaRepository,
            LoreJpaRepository loreJpaRepository) {
        this.characterJpaRepository = characterJpaRepository;
        this.placeJpaRepository = placeJpaRepository;
        this.factionJpaRepository = factionJpaRepository;
        this.itemJpaRepository = itemJpaRepository;
        this.eventJpaRepository = eventJpaRepository;
        this.loreJpaRepository = loreJpaRepository;
    }

    @Override
    public Optional<EntityLinkSummary> findSummary(EntityType type, UUID id) {
        return switch (type) {
            case CHARACTER -> characterJpaRepository.findById(id)
                    .map(e -> new EntityLinkSummary(type, e.getId(), e.getName(), e.getStatus()));
            case PLACE -> placeJpaRepository.findById(id)
                    .map(e -> new EntityLinkSummary(type, e.getId(), e.getName(), e.getStatus()));
            case FACTION -> factionJpaRepository.findById(id)
                    .map(e -> new EntityLinkSummary(type, e.getId(), e.getName(), e.getStatus()));
            case ITEM -> itemJpaRepository.findById(id)
                    .map(e -> new EntityLinkSummary(type, e.getId(), e.getName(), e.getStatus()));
            case EVENT -> eventJpaRepository.findById(id)
                    .map(e -> new EntityLinkSummary(type, e.getId(), e.getName(), e.getStatus()));
            case LORE -> loreJpaRepository.findById(id)
                    .map(e -> new EntityLinkSummary(type, e.getId(), e.getName(), e.getStatus()));
        };
    }
}
