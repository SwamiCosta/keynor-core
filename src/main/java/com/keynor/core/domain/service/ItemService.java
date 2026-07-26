package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.EntityType;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.item.*;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.ItemRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ItemService implements
        CreateItemUseCase,
        UpdateItemUseCase,
        ChangeItemStatusUseCase,
        DeleteItemUseCase,
        FindItemByIdUseCase,
        FindAllItemsUseCase {

    private final ItemRepository itemRepository;
    private final EntityLinkRepository entityLinkRepository;
    private final EraRepository eraRepository;
    private final UniverseEntityLookupRepository universeEntityLookupRepository;
    private final CreateHiddenContentLockUseCase createHiddenContentLockUseCase;

    public ItemService(
            ItemRepository itemRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository,
            UniverseEntityLookupRepository universeEntityLookupRepository,
            CreateHiddenContentLockUseCase createHiddenContentLockUseCase) {
        this.itemRepository = itemRepository;
        this.entityLinkRepository = entityLinkRepository;
        this.eraRepository = eraRepository;
        this.universeEntityLookupRepository = universeEntityLookupRepository;
        this.createHiddenContentLockUseCase = createHiddenContentLockUseCase;
    }

    @Override
    public Item create(CreateItemUseCase.Command command) {
        if (itemRepository.existsByNameAndLanguage(command.name(), command.language())) {
            throw new DuplicateEntityNameException("Item", command.name());
        }
        validateTimeline(command.timeline());
        Instant now = Instant.now();
        EntityStatus initialStatus = command.status() != null ? command.status() : EntityStatus.DRAFT;
        UUID newId = UUID.randomUUID();
        UUID translationGroupId = command.translationGroupId() != null ? command.translationGroupId() : newId;
        Item item = new Item(
                newId,
                command.name(),
                command.summary(),
                command.body(),
                command.images(),
                command.categories(),
                initialStatus,
                command.timeline(),
                now,
                now,
                command.language(),
                translationGroupId,
                command.hidden());
        Item saved = itemRepository.save(item);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.ITEM, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.ITEM, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
    }

    @Override
    public Item update(UUID id, UpdateItemUseCase.Command command) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        validateTimeline(command.timeline());
        if (command.hidden() && (command.riddleText() == null || command.riddleText().isBlank()
                || command.password() == null || command.password().isBlank())) {
            throw new IllegalArgumentException("riddleText and password are required when hidden is true");
        }
        item.update(command.name(), command.summary(), command.body(), command.images(), command.categories(), command.timeline());
        item.setHidden(command.hidden());
        Item saved = itemRepository.save(item);
        List<com.keynor.core.domain.model.shared.EntityLinkRef> links = command.links() != null ? command.links() : List.of();
        HiddenLinkDirectionValidator.validate(saved.isHidden(), links, universeEntityLookupRepository);
        entityLinkRepository.replaceLinks(EntityType.ITEM, saved.getId(), links);
        if (saved.isHidden()) {
            createHiddenContentLockUseCase.createOrReplace(EntityType.ITEM, saved.getId(), command.riddleText(), command.password());
        }
        return saved;
    }

    @Override
    public Item changeStatus(UUID id, EntityStatus newStatus) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.changeStatus(newStatus);
        return itemRepository.save(item);
    }

    @Override
    public void delete(UUID id) {
        if (!itemRepository.existsById(id)) {
            throw new EntityNotFoundException("Item", id);
        }
        itemRepository.deleteById(id);
        entityLinkRepository.deleteAllForEntity(EntityType.ITEM, id);
    }

    @Override
    public Item findById(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
    }

    @Override
    public PageResult<Item> findAll(EntityFilter filter, PageRequest pageRequest) {
        return itemRepository.findAll(filter, pageRequest);
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
