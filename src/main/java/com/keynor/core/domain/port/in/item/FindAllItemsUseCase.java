package com.keynor.core.domain.port.in.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;

public interface FindAllItemsUseCase {
    PageResult<Item> findAll(EntityFilter filter, PageRequest pageRequest);
}
