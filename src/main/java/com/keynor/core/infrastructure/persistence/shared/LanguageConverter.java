package com.keynor.core.infrastructure.persistence.shared;

import com.keynor.core.domain.model.shared.Language;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LanguageConverter implements AttributeConverter<Language, String> {

    @Override
    public String convertToDatabaseColumn(Language language) {
        return language == null ? null : language.name().toLowerCase();
    }

    @Override
    public Language convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : Language.valueOf(dbValue.toUpperCase());
    }
}
