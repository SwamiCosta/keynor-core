package com.keynor.core.infrastructure.web.shared;

import com.keynor.core.domain.model.shared.EntityStatus;

public final class EntityStatusRequestParser {

    private EntityStatusRequestParser() {
    }

    public static EntityStatus parseCreationStatus(String rawStatus) {
        if (rawStatus == null) {
            return EntityStatus.DRAFT;
        }
        EntityStatus status = EntityStatus.valueOf(rawStatus.toUpperCase());
        if (status == EntityStatus.DEPRECATED) {
            throw new IllegalArgumentException("Status DEPRECATED is not allowed on creation. Allowed values: DRAFT, CANON");
        }
        return status;
    }
}
