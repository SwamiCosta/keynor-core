package com.keynor.core.application.dto.map;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * {@code name}: blank/absent leaves the pin's current name untouched (a pure
 * reposition never has to resend it); a non-blank value renames the pin.
 * There is currently no way to explicitly clear a custom name back to
 * deriving from the linked entity -- see MapPinService. {@code entityType}/
 * {@code entityId}: both absent leaves the pin's current link untouched;
 * both present attaches/re-targets the pin to that entity. One without the
 * other is rejected -- see MapPinService. {@code shape}: absent leaves the
 * pin's current shape untouched, same as {@code name}.
 */
public record UpdateMapPinRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double normalizedX,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double normalizedY,
        String name,
        String entityType,
        UUID entityId,
        String shape) {
}
