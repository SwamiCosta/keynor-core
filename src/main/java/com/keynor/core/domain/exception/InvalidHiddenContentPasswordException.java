package com.keynor.core.domain.exception;

public class InvalidHiddenContentPasswordException extends RuntimeException {

    public InvalidHiddenContentPasswordException() {
        super("The provided password does not unlock this hidden content");
    }
}
