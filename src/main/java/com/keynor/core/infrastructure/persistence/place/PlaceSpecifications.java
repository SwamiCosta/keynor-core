package com.keynor.core.infrastructure.persistence.place;

import com.keynor.core.domain.model.place.PlaceCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PlaceSpecifications {

    private PlaceSpecifications() {}

    public static Specification<PlaceEntity> fromFilter(EntityFilter filter) {
        Specification<PlaceEntity> spec = Specification.where(hasLanguage(filter.language()));

        if (filter.hasStatusFilter()) {
            spec = spec.and(hasAnyStatus(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<PlaceCategory> categories = filter.categories().stream()
                    .map(PlaceCategory::valueOf)
                    .toList();
            spec = spec.and(hasAnyCategory(categories));
        }

        return spec;
    }

    private static Specification<PlaceEntity> hasLanguage(Language language) {
        return (root, query, cb) -> cb.equal(root.get("language"), language);
    }

    private static Specification<PlaceEntity> hasAnyStatus(List<EntityStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    private static Specification<PlaceEntity> hasAnyCategory(List<PlaceCategory> categories) {
        return (root, query, cb) -> root.join("categories").in(categories);
    }
}
