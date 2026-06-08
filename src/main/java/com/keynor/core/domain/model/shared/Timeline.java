package com.keynor.core.domain.model.shared;

public record Timeline(String founded, String destroyed) {
    public Timeline {
        if (founded == null || founded.isBlank()) {
            throw new IllegalArgumentException("Timeline founded era must not be null or blank");
        }
    }
}
