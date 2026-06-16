package com.keynor.core.infrastructure.config;

import com.keynor.core.domain.port.out.*;
import com.keynor.core.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public CharacterService characterService(CharacterRepository characterRepository, EntityLinkRepository entityLinkRepository) {
        return new CharacterService(characterRepository, entityLinkRepository);
    }

    @Bean
    public PlaceService placeService(PlaceRepository placeRepository, EntityLinkRepository entityLinkRepository) {
        return new PlaceService(placeRepository, entityLinkRepository);
    }

    @Bean
    public FactionService factionService(FactionRepository factionRepository, EntityLinkRepository entityLinkRepository) {
        return new FactionService(factionRepository, entityLinkRepository);
    }

    @Bean
    public ItemService itemService(ItemRepository itemRepository, EntityLinkRepository entityLinkRepository) {
        return new ItemService(itemRepository, entityLinkRepository);
    }

    @Bean
    public EventService eventService(EventRepository eventRepository, EntityLinkRepository entityLinkRepository) {
        return new EventService(eventRepository, entityLinkRepository);
    }

    @Bean
    public LoreService loreService(LoreRepository loreRepository, EntityLinkRepository entityLinkRepository) {
        return new LoreService(loreRepository, entityLinkRepository);
    }

    @Bean
    public EntityLinkService entityLinkService(
            EntityLinkRepository entityLinkRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository) {
        return new EntityLinkService(entityLinkRepository, universeEntityLookupRepository);
    }

    @Bean
    public EraService eraService(EraRepository eraRepository) {
        return new EraService(eraRepository);
    }

    @Bean
    public MapService mapService(MapRepository mapRepository) {
        return new MapService(mapRepository);
    }
}
