package com.keynor.core.infrastructure.web.shared;

import com.keynor.core.domain.model.shared.Language;

public final class LanguageRequestParser {

    private LanguageRequestParser() {
    }

    public static Language parse(String rawLanguage) {
        try {
            return Language.valueOf(rawLanguage.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported language '" + rawLanguage + "'. Allowed values: EN, PT");
        }
    }
}
