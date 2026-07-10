package com.keynor.core.infrastructure.web.archetype;

import com.keynor.core.application.dto.archetype.ArchetypeResponse;
import com.keynor.core.domain.model.archetype.Archetype;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.port.in.archetype.FindAllArchetypesUseCase;
import com.keynor.core.domain.port.in.archetype.FindArchetypeByIdUseCase;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicArchetypeControllerTest {

    @Mock
    private FindAllArchetypesUseCase findAllArchetypesUseCase;

    @Mock
    private FindArchetypeByIdUseCase findArchetypeByIdUseCase;

    private PublicArchetypeController controller;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        controller = new PublicArchetypeController(findAllArchetypesUseCase, findArchetypeByIdUseCase);
    }

    @Test
    void findAll_shouldReturnMappedList() {
        UUID obsessionId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();

        Archetype obsession = new Archetype(
                obsessionId, "Obsession", null, null, null, null, null, null, "The obsessive archetype", NOW, NOW, Language.EN, UUID.randomUUID());
        Archetype theSeeker = new Archetype(
                seekerId, "The Seeker", "Air", "Wands", "Explorer", "Choleric", "Intuition", "Outward",
                "The seeking archetype", NOW, NOW, Language.EN, UUID.randomUUID());

        when(findAllArchetypesUseCase.findAll(Language.EN)).thenReturn(List.of(obsession, theSeeker));

        var response = controller.findAll("en");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<ArchetypeResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(2);

        ArchetypeResponse firstEntry = body.get(0);
        assertThat(firstEntry.id()).isEqualTo(obsessionId);
        assertThat(firstEntry.name()).isEqualTo("Obsession");
        assertThat(firstEntry.element()).isNull();
        assertThat(firstEntry.suit()).isNull();

        ArchetypeResponse secondEntry = body.get(1);
        assertThat(secondEntry.id()).isEqualTo(seekerId);
        assertThat(secondEntry.name()).isEqualTo("The Seeker");
        assertThat(secondEntry.element()).isEqualTo("Air");
        assertThat(secondEntry.suit()).isEqualTo("Wands");

        verify(findAllArchetypesUseCase).findAll(Language.EN);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntriesExist() {
        when(findAllArchetypesUseCase.findAll(Language.EN)).thenReturn(List.of());

        var response = controller.findAll("en");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void findById_shouldReturnArchetype_whenFound() {
        UUID id = UUID.randomUUID();
        Archetype archetype = new Archetype(
                id, "The Seeker", "Air", "Wands", "Explorer", "Choleric", "Intuition", "Outward",
                "The seeking archetype", NOW, NOW, Language.EN, UUID.randomUUID());
        when(findArchetypeByIdUseCase.findById(id)).thenReturn(archetype);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        assertThat(response.getBody().name()).isEqualTo("The Seeker");
        assertThat(response.getBody().element()).isEqualTo("Air");
        verify(findArchetypeByIdUseCase).findById(id);
    }
}
