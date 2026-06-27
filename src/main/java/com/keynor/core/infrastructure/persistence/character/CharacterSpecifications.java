package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CharacterSpecifications {

    private CharacterSpecifications() {}

    public static Specification<CharacterEntity> fromFilter(EntityFilter filter) {
        Specification<CharacterEntity> spec = Specification.where(null);

        if (filter.hasStatusFilter()) {
            spec = spec.and(hasAnyStatus(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<CharacterCategory> categories = filter.categories().stream()
                    .map(CharacterCategory::valueOf)
                    .toList();
            spec = spec.and(hasAnyCategory(categories));
        }

        return spec;
    }

    private static Specification<CharacterEntity> hasAnyStatus(List<EntityStatus> statuses) {
        return (root, query, cb) -> root.get("status").in(statuses);
    }

    private static Specification<CharacterEntity> hasAnyCategory(List<CharacterCategory> categories) {
        return (root, query, cb) -> {
            var categoriesJoin = root.join("categories");
            return categoriesJoin.in(categories);
        };
    }
}
