package com.keynor.core.application.dto.map;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * {@code name}: full-replace, blank/absent clears any custom override (falls
 * back to the linked entity's live name, if any). {@code entityType}/{@code
 * entityId}: both absent leaves the pin's current link untouched; both
 * present attaches/re-targets the pin to that entity. One without the other
 * is rejected -- see MapPinService.
 */
public record UpdateMapPinRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double normalizedX,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double normalizedY,
        String name,
        String entityType,
        UUID entityId) {
}
