package com.keynor.core.infrastructure.config;

import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.*;
import com.keynor.core.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public CharacterService characterService(
            CharacterRepository characterRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository,
            CreateHiddenContentLockUseCase createHiddenContentLockUseCase) {
        return new CharacterService(characterRepository, entityLinkRepository, eraRepository,
                universeEntityLookupRepository, createHiddenContentLockUseCase);
    }

    @Bean
    public PlaceService placeService(
            PlaceRepository placeRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository) {
        return new PlaceService(placeRepository, entityLinkRepository, eraRepository);
    }

    @Bean
    public FactionService factionService(
            FactionRepository factionRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository) {
        return new FactionService(factionRepository, entityLinkRepository, eraRepository);
    }

    @Bean
    public ItemService itemService(
            ItemRepository itemRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository) {
        return new ItemService(itemRepository, entityLinkRepository, eraRepository);
    }

    @Bean
    public EventService eventService(
            EventRepository eventRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository) {
        return new EventService(eventRepository, entityLinkRepository, eraRepository);
    }

    @Bean
    public LoreService loreService(
            LoreRepository loreRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository,
            CreateHiddenContentLockUseCase createHiddenContentLockUseCase) {
        return new LoreService(loreRepository, entityLinkRepository, eraRepository,
                universeEntityLookupRepository, createHiddenContentLockUseCase);
    }

    @Bean
    public HiddenContentService hiddenContentService(
            HiddenContentLockRepository hiddenContentLockRepository,
            PasswordHasher passwordHasher,
            HiddenUnlockTokenSigner hiddenUnlockTokenSigner) {
        return new HiddenContentService(hiddenContentLockRepository, passwordHasher, hiddenUnlockTokenSigner);
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

    @Bean
    public MapPinService mapPinService(
            MapPinRepository mapPinRepository,
            MapRepository mapRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository) {
        return new MapPinService(mapPinRepository, mapRepository, universeEntityLookupRepository);
    }

    @Bean
    public ArchetypeService archetypeService(ArchetypeRepository archetypeRepository) {
        return new ArchetypeService(archetypeRepository);
    }

    @Bean
    public SignService signService(SignRepository signRepository) {
        return new SignService(signRepository);
    }
}
