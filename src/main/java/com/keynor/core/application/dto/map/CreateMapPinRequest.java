package com.keynor.core.application.dto.map;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateMapPinRequest(
        String entityType,
        UUID entityId,
        String name,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double normalizedX,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double normalizedY) {
}
