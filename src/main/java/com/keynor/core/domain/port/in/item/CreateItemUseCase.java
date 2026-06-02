package com.keynor.core.domain.port.in.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateItemUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> tags,
            List<ItemCategory> categories,
            Timeline timeline) {}

    Item create(Command command);
}
