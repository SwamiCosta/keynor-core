package com.keynor.core.domain.port.in.place;

import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface CreatePlaceUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<PlaceCategory> categories,
            MapType mapType,
            Timeline timeline,
            EntityStatus status,
            Language language,
            UUID translationGroupId,
            UUID versionGroupId,
            List<EntityLinkRef> links,
            boolean hidden,
            String riddleText,
            String password) {}

    Place create(Command command);
}
