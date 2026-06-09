package com.keynor.core.domain.port.in.place;

import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.model.place.Place;
import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface UpdatePlaceUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> tags,
            List<String> images,
            List<PlaceCategory> categories,
            MapType mapType,
            Timeline timeline) {}

    Place update(UUID id, Command command);
}
