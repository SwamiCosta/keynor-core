package com.keynor.core.infrastructure.persistence.faction;

import com.keynor.core.domain.model.faction.FactionCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class FactionSpecifications {

    private FactionSpecifications() {}

    public static Specification<FactionEntity> fromFilter(EntityFilter filter) {
        Specification<FactionEntity> spec = Specification.where(hasLanguage(filter.language()));

        if (filter.hasStatusFilter()) {
            spec = spec.and((root, query, cb) -> root.get("status").in(filter.statuses()));
        }
        if (filter.hasCategoryFilter()) {
            List<FactionCategory> categories = filter.categories().stream()
                    .map(FactionCategory::valueOf)
                    .toList();
            spec = spec.and((root, query, cb) -> root.join("categories").in(categories));
        }
        if (filter.excludeHidden()) {
            spec = spec.and((root, query, cb) -> cb.isFalse(root.get("hidden")));
        }

        return spec;
    }

    private static Specification<FactionEntity> hasLanguage(Language language) {
        return (root, query, cb) -> cb.equal(root.get("language"), language);
    }
}
