package com.keynor.core.domain.port.in.item;

import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface CreateItemUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<ItemCategory> categories,
            Timeline timeline,
            EntityStatus status,
            Language language,
            UUID translationGroupId,
            UUID versionGroupId,
            List<EntityLinkRef> links,
            boolean hidden,
            String riddleText,
            String password) {}

    Item create(Command command);
}
