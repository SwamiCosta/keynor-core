package com.keynor.core.infrastructure.config;

import com.keynor.core.domain.port.out.*;
import com.keynor.core.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public CharacterService characterService(CharacterRepository characterRepository) {
        return new CharacterService(characterRepository);
    }

    @Bean
    public PlaceService placeService(PlaceRepository placeRepository) {
        return new PlaceService(placeRepository);
    }

    @Bean
    public FactionService factionService(FactionRepository factionRepository) {
        return new FactionService(factionRepository);
    }

    @Bean
    public ItemService itemService(ItemRepository itemRepository) {
        return new ItemService(itemRepository);
    }

    @Bean
    public EventService eventService(EventRepository eventRepository) {
        return new EventService(eventRepository);
    }

    @Bean
    public LoreService loreService(LoreRepository loreRepository) {
        return new LoreService(loreRepository);
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
