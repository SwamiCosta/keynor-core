package com.keynor.core.domain.exception;

public class HiddenContentAccessDeniedException extends RuntimeException {

    public HiddenContentAccessDeniedException() {
        super("Missing, invalid, or expired unlock token for this hidden content");
    }
}
