package com.keynor.core.domain.port.in.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityLinkRef;
import com.keynor.core.domain.model.shared.Timeline;

import java.util.List;
import java.util.UUID;

public interface UpdateCharacterUseCase {

    record Command(
            String name,
            String summary,
            String body,
            List<String> images,
            List<CharacterCategory> categories,
            Timeline timeline,
            List<EntityLinkRef> links,
            boolean hidden,
            String riddleText,
            String password,
            boolean common) {}

    Character update(UUID id, Command command);
}
