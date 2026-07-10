package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

import java.util.Optional;
import java.util.UUID;

public interface ItemRepository {
    Item save(Item item);
    Optional<Item> findById(UUID id);
    boolean existsById(UUID id);
    boolean existsByNameAndLanguage(String name, Language language);
    void deleteById(UUID id);
    PageResult<Item> findAll(EntityFilter filter, PageRequest pageRequest);
}
