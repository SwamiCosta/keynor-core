package com.keynor.core.infrastructure.persistence.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.out.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ItemJpaAdapter implements ItemRepository {

    private final ItemJpaRepository jpaRepository;
    private final ItemMapper mapper;

    public ItemJpaAdapter(ItemJpaRepository jpaRepository, ItemMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Item save(Item item) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(item)));
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public boolean existsById(UUID id) { return jpaRepository.existsById(id); }

    @Override
    public boolean existsByName(String name) { return jpaRepository.existsByName(name); }

    @Override
    public void deleteById(UUID id) { jpaRepository.deleteById(id); }

    @Override
    public PageResult<Item> findAll(EntityFilter filter, com.keynor.core.domain.model.shared.PageRequest pageRequest) {
        Page<ItemEntity> page = jpaRepository.findAll(
                ItemSpecifications.fromFilter(filter),
                PageRequest.of(pageRequest.page(), pageRequest.size()));
        return new PageResult<>(
                page.getContent().stream().map(mapper::toDomain).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
