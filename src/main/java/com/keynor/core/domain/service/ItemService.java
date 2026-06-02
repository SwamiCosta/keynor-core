package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.item.*;
import com.keynor.core.domain.port.out.ItemRepository;

import java.time.Instant;
import java.util.UUID;

public class ItemService implements
        CreateItemUseCase,
        UpdateItemUseCase,
        ChangeItemStatusUseCase,
        DeleteItemUseCase,
        FindItemByIdUseCase,
        FindAllItemsUseCase {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public Item create(CreateItemUseCase.Command command) {
        if (itemRepository.existsByName(command.name())) {
            throw new DuplicateEntityNameException("Item", command.name());
        }
        Instant now = Instant.now();
        Item item = new Item(
                UUID.randomUUID(),
                command.name(),
                command.summary(),
                command.body(),
                command.tags(),
                command.categories(),
                EntityStatus.DRAFT,
                command.timeline(),
                now,
                now);
        return itemRepository.save(item);
    }

    @Override
    public Item update(UUID id, UpdateItemUseCase.Command command) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Item", id));
        item.update(command.name(), command.summary(), command.body(), command.tags(), command.categories(), command.timeline());
        return itemRepository.save(item);
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
}
