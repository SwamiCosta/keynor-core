package com.keynor.core.domain.model.shared;

import java.util.UUID;

public record EntityLinkRef(EntityType targetType, UUID targetId) {
}
