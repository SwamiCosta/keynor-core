package com.keynor.core.domain.exception;

public class UnknownEraNameException extends RuntimeException {

    public UnknownEraNameException(String eraName) {
        super("No era found with name: " + eraName);
    }
}
