package com.keynor.core.domain.port.in.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateItemUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> tags,
            List<String> images,
            List<ItemCategory> categories,
            Timeline timeline,
            List<EntityLinkRef> links) {}

    Item create(Command command);
}
