package com.keynor.core.infrastructure.persistence.character;

import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.port.out.CharacterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CharacterJpaAdapterTest {

    @Autowired
    private CharacterRepository characterRepository;

    @Test
    void save_shouldPersistAndRetrieveImagesInOrder() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        List<String> images = List.of(
                "https://example.com/image1.png",
                "https://example.com/image2.png");
        Character character = new Character(
                id, "Araveth", "A hero", "Long body",
                images,
                List.of(CharacterCategory.HERO),
                EntityStatus.DRAFT, null, now, now, com.keynor.core.domain.model.shared.Language.EN, UUID.randomUUID(), false);

        characterRepository.save(character);

        Character retrieved = characterRepository.findById(id).orElseThrow();
        assertThat(retrieved.getImages()).hasSize(2);
        assertThat(retrieved.getImages().get(0)).isEqualTo("https://example.com/image1.png");
        assertThat(retrieved.getImages().get(1)).isEqualTo("https://example.com/image2.png");
    }
}
