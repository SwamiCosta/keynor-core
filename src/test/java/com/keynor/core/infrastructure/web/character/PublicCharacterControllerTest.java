package com.keynor.core.infrastructure.web.character;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.character.FindAllCharactersUseCase;
import com.keynor.core.domain.port.in.character.FindCharacterByIdUseCase;
import com.keynor.core.domain.port.in.character.FindCharactersByIdsUseCase;
import com.keynor.core.domain.port.in.shared.FindLinkedEntitiesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicCharacterControllerTest {

    @Mock
    private FindAllCharactersUseCase findAllCharactersUseCase;

    @Mock
    private FindCharacterByIdUseCase findCharacterByIdUseCase;

    @Mock
    private FindCharactersByIdsUseCase findCharactersByIdsUseCase;

    @Mock
    private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private PublicCharacterController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicCharacterController(
                findAllCharactersUseCase, findCharacterByIdUseCase, findCharactersByIdsUseCase, findLinkedEntitiesUseCase);
    }

    @Test
    void findAll_responseBodyShouldIncludeImagesField() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        List<String> images = List.of("https://example.com/araveth.png");
        Character character = new Character(
                id, "Araveth", "A wandering hero", "Body",
                images,
                List.of(CharacterCategory.HERO),
                EntityStatus.CANON, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(character), 0, 20, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll("en", null, 0, 20);

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
                images,
                List.of(CharacterCategory.HERO),
                EntityStatus.CANON, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
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
                List.of(),
                List.of(CharacterCategory.COMPANION),
                EntityStatus.CANON, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(character), 0, 20, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll("en", null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content().get(0).images()).isEmpty();
    }

    @Test
    void findAll_shouldAlwaysExcludeCommonEntities() {
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll("en", null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllCharactersUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().excludeCommon()).isTrue();
    }

    @Test
    void findByIds_shouldDelegateToUseCaseWithProvidedIds() {
        UUID id = UUID.randomUUID();
        when(findCharactersByIdsUseCase.findByIds(List.of(id))).thenReturn(List.of());

        var response = controller.findByIds(List.of(id));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(findCharactersByIdsUseCase).findByIds(List.of(id));
    }

    @Test
    void findByIds_shouldReturnMappedResponseIncludingNonCanonEntities() {
        Instant now = Instant.now();
        UUID canonId = UUID.randomUUID();
        UUID draftId = UUID.randomUUID();
        Character canonCharacter = new Character(canonId, "Araveth", null, null, List.of(),
                List.of(CharacterCategory.HERO), EntityStatus.CANON, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        Character draftCharacter = new Character(draftId, "Unfinished One", null, null, List.of(),
                List.of(CharacterCategory.COMPANION), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(findCharactersByIdsUseCase.findByIds(List.of(canonId, draftId)))
                .thenReturn(List.of(canonCharacter, draftCharacter));

        var response = controller.findByIds(List.of(canonId, draftId));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).status()).isEqualTo("CANON");
        assertThat(response.getBody().get(1).status()).isEqualTo("DRAFT");
        assertThat(response.getBody().get(1).name()).isEqualTo("Unfinished One");
    }
}
