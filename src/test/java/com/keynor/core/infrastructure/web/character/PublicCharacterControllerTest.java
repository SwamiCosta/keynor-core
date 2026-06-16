package com.keynor.core.infrastructure.web.character;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.character.FindAllCharactersUseCase;
import com.keynor.core.domain.port.in.character.FindCharacterByIdUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCharacterControllerTest {

    @Mock
    private FindAllCharactersUseCase findAllCharactersUseCase;

    @Mock
    private FindCharacterByIdUseCase findCharacterByIdUseCase;

    @Mock
    private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private PublicCharacterController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicCharacterController(
                findAllCharactersUseCase, findCharacterByIdUseCase, findLinkedEntitiesUseCase);
    }

    @Test
    void findAll_responseBodyShouldIncludeImagesField() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        List<String> images = List.of("https://example.com/araveth.png");
        Character character = new Character(
                id, "Araveth", "A wandering hero", "Body",
                List.of("hero"), images,
                List.of(CharacterCategory.HERO),
                EntityStatus.CANON, null, now, now);
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(character), 0, 20, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<CharacterResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.content()).hasSize(1);
        assertThat(body.content().get(0).images()).containsExactly("https://example.com/araveth.png");
    }

    @Test
    void findById_responseBodyShouldIncludeImagesField() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        List<String> images = List.of(
                "https://example.com/img1.png",
                "https://example.com/img2.png");
        Character character = new Character(
                id, "Araveth", "A wandering hero", "Body",
                List.of(), images,
                List.of(CharacterCategory.HERO),
                EntityStatus.CANON, null, now, now);
        when(findCharacterByIdUseCase.findById(id)).thenReturn(character);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().images()).containsExactly(
                "https://example.com/img1.png",
                "https://example.com/img2.png");
    }

    @Test
    void findAll_responseBodyShouldIncludeEmptyImagesArray_whenNoImagesSet() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Character character = new Character(
                id, "Araveth", null, null,
                List.of(), List.of(),
                List.of(CharacterCategory.NPC),
                EntityStatus.CANON, null, now, now);
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(character), 0, 20, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content().get(0).images()).isEmpty();
    }
}
