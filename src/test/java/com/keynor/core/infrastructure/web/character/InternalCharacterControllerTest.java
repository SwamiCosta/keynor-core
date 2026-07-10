package com.keynor.core.infrastructure.web.character;

import com.keynor.core.application.dto.character.CharacterResponse;
import com.keynor.core.application.dto.character.CreateCharacterRequest;
import com.keynor.core.application.dto.character.UpdateCharacterRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.character.Character;
import com.keynor.core.domain.model.character.CharacterCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.character.*;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalCharacterControllerTest {

    @Mock private CreateCharacterUseCase createCharacterUseCase;
    @Mock private UpdateCharacterUseCase updateCharacterUseCase;
    @Mock private ChangeCharacterStatusUseCase changeCharacterStatusUseCase;
    @Mock private DeleteCharacterUseCase deleteCharacterUseCase;
    @Mock private FindCharacterByIdUseCase findCharacterByIdUseCase;
    @Mock private FindAllCharactersUseCase findAllCharactersUseCase;
    @Mock private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private InternalCharacterController controller;

    private Character buildCharacter(UUID id) {
        Instant now = Instant.now();
        return new Character(id, "Araveth", "A hero", "Body",
                List.of(), List.of(CharacterCategory.HERO), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID());
    }

    @BeforeEach
    void setUp() {
        controller = new InternalCharacterController(
                createCharacterUseCase, updateCharacterUseCase, changeCharacterStatusUseCase,
                deleteCharacterUseCase, findCharacterByIdUseCase, findAllCharactersUseCase,
                findLinkedEntitiesUseCase);
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        Character character = buildCharacter(id);
        when(createCharacterUseCase.create(any())).thenReturn(character);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateCharacterRequest("Araveth", "A hero", "Body",
                List.of(), List.of("HERO"), "era-1", null, null,"en", null, null);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Araveth");
        verify(createCharacterUseCase).create(any());
    }

    @Test
    void create_shouldPassCorrectCommandToUseCase() {
        UUID id = UUID.randomUUID();
        when(createCharacterUseCase.create(any())).thenReturn(buildCharacter(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateCharacterRequest("Araveth", "A hero", "Body",
                List.of("img.png"), List.of("HERO"), "era-1", null, null,"en", null, null);

        controller.create(request);

        ArgumentCaptor<CreateCharacterUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateCharacterUseCase.Command.class);
        verify(createCharacterUseCase).create(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Araveth");
        assertThat(captor.getValue().categories()).containsExactly(CharacterCategory.HERO);
    }

    @Test
    void create_shouldDefaultToDraftStatus_whenStatusIsNull() {
        UUID id = UUID.randomUUID();
        when(createCharacterUseCase.create(any())).thenReturn(buildCharacter(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateCharacterRequest("Araveth", "A hero", "Body",
                List.of(), List.of("HERO"), "era-1", null, null,"en", null, null);

        controller.create(request);

        ArgumentCaptor<CreateCharacterUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateCharacterUseCase.Command.class);
        verify(createCharacterUseCase).create(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void create_shouldPassCanonStatus_whenStatusIsCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Character canonCharacter = new Character(id, "Araveth", null, null, List.of(),
                List.of(CharacterCategory.HERO), EntityStatus.CANON, null, now, now, Language.EN, UUID.randomUUID());
        when(createCharacterUseCase.create(any())).thenReturn(canonCharacter);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateCharacterRequest("Araveth", "A hero", "Body",
                List.of(), List.of("HERO"), "era-1", null, "CANON","en", null, null);

        controller.create(request);

        ArgumentCaptor<CreateCharacterUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateCharacterUseCase.Command.class);
        verify(createCharacterUseCase).create(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenStatusIsDeprecated() {
        var request = new CreateCharacterRequest("Araveth", "A hero", "Body",
                List.of(), List.of("HERO"), "era-1", null, "DEPRECATED","en", null, null);

        assertThatThrownBy(() -> controller.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEPRECATED");
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        Character updated = buildCharacter(id);
        when(updateCharacterUseCase.update(eq(id), any())).thenReturn(updated);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new UpdateCharacterRequest("Araveth Updated", "New summary", "New body",
                List.of(), List.of("HERO"), "era-1", null, null);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(updateCharacterUseCase).update(eq(id), any());
    }

    @Test
    void changeStatus_shouldReturn200AndCallUseCase() {
        UUID id = UUID.randomUUID();
        Character character = buildCharacter(id);
        when(changeCharacterStatusUseCase.changeStatus(id, EntityStatus.CANON)).thenReturn(character);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.changeStatus(id, new ChangeStatusRequest("CANON"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(changeCharacterStatusUseCase).changeStatus(id, EntityStatus.CANON);
    }

    @Test
    void delete_shouldReturn204AndCallUseCase() {
        UUID id = UUID.randomUUID();

        var response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteCharacterUseCase).delete(id);
    }

    @Test
    void findAll_shouldPassPaginationAndReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        Character character = buildCharacter(id);
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(character), 1, 10, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll("en", null, null, 1, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<CharacterResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllCharactersUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(1);
        assertThat(pageCaptor.getValue().size()).isEqualTo(10);
    }

    @Test
    void findAll_shouldNotApplyCanonFilter_whenNoStatusProvided() {
        when(findAllCharactersUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll("en", null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllCharactersUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).isEmpty();
    }

    @Test
    void findById_shouldDelegateAndMapResult() {
        UUID id = UUID.randomUUID();
        Character character = buildCharacter(id);
        when(findCharacterByIdUseCase.findById(id)).thenReturn(character);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findCharacterByIdUseCase).findById(id);
    }
}
