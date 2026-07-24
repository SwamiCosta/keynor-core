package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CharacterSpecifications {

    private CharacterSpecifications() {}

    public static Specification<CharacterEntity> fromFilter(EntityFilter filter) {
        Specification<CharacterEntity> spec = Specification.where(hasLanguage(filter.language()));

        if (filter.hasStatusFilter()) {
            spec = spec.and(hasAnyStatus(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<CharacterCategory> categories = filter.categories().stream()
                    .map(CharacterCategory::valueOf)
                    .toList();
            spec = spec.and(hasAnyCategory(categories));
        }
        if (filter.excludeHidden()) {
            spec = spec.and(isNotHidden());
        }

        return spec;
    }

    private static Specification<CharacterEntity> isNotHidden() {
        return (root, query, cb) -> cb.isFalse(root.get("hidden"));
    }

    private static Specification<CharacterEntity> hasLanguage(Language language) {
        return (root, query, cb) -> cb.equal(root.get("language"), language);
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
