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
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.ItemRepository;

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

    public ItemService(
            ItemRepository itemRepository,
            EntityLinkRepository entityLinkRepository,
            EraRepository eraRepository) {
        this.itemRepository = itemRepository;
        this.entityLinkRepository = entityLinkRepository;
        this.eraRepository = eraRepository;
    }

    @Override
    public Item create(CreateItemUseCase.Command command) {
        if (itemRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Item", command.name());
        }
        validateTimeline(command.timeline());
        Instant now = Instant.now();
        Item item = new Item(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.images(),
                command.categories(),
                EntityStatus.DRAFT,
                command.timeline(),
                now,
                now);
        Item saved = itemRepository.save(item);
        entityLinkRepository.replaceLinks(EntityType.ITEM, saved.getId(), command.links() != null ? command.links() : List.of());
        return saved;
    }

    @Override
    public Item update(UUID id, UpdateItemUseCase.Command command) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        validateTimeline(command.timeline());
        item.update(command.name(), command.summary(), command.body(), command.images(), command.categories(), command.timeline());
        Item saved = itemRepository.save(item);
        entityLinkRepository.replaceLinks(EntityType.ITEM, saved.getId(), command.links() != null ? command.links() : List.of());
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
