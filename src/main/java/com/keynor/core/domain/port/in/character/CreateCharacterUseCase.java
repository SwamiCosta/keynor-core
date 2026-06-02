package com.keynor.core.domain.port.in.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;

public interface CreateCharacterUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> tags,
            List<CharacterCategory> categories,
            Timeline timeline) {}

    Character create(Command command);
}
