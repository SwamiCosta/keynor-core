package com.keynor.core.infrastructure.web.sign;

import com.keynor.core.application.dto.sign.SignResponse;
import com.keynor.core.domain.model.sign.Sign;
import com.keynor.core.domain.port.in.sign.FindAllSignsUseCase;
import com.keynor.core.domain.port.in.sign.FindSignByIdUseCase;
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
class PublicSignControllerTest {

    @Mock
    private FindAllSignsUseCase findAllSignsUseCase;

    @Mock
    private FindSignByIdUseCase findSignByIdUseCase;

    private PublicSignController controller;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        controller = new PublicSignController(findAllSignsUseCase, findSignByIdUseCase);
    }

    @Test
    void findAll_shouldReturnMappedList_inSignOrderSequence() {
        UUID riftId = UUID.randomUUID();
        UUID seekerId = UUID.randomUUID();

        Sign rift = new Sign(
                riftId, "The Rift", 1, "Deep Winter", UUID.randomUUID(), null,
                "The rift summary", "The rift body", NOW, NOW);
        Sign seeker = new Sign(
                seekerId, "The Seeker", 2, "Early Spring", UUID.randomUUID(), "The Wanderer",
                "The seeker summary", "The seeker body", NOW, NOW);

        when(findAllSignsUseCase.findAll()).thenReturn(List.of(rift, seeker));

        var response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<SignResponse> body = response.getBody();
        assertThat(body).isNotNull().hasSize(2);

        SignResponse firstEntry = body.get(0);
        assertThat(firstEntry.id()).isEqualTo(riftId);
        assertThat(firstEntry.name()).isEqualTo("The Rift");
        assertThat(firstEntry.signOrder()).isEqualTo(1);
        assertThat(firstEntry.subArchetype()).isNull();

        SignResponse secondEntry = body.get(1);
        assertThat(secondEntry.id()).isEqualTo(seekerId);
        assertThat(secondEntry.name()).isEqualTo("The Seeker");
        assertThat(secondEntry.signOrder()).isEqualTo(2);
        assertThat(secondEntry.subArchetype()).isEqualTo("The Wanderer");

        verify(findAllSignsUseCase).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntriesExist() {
        when(findAllSignsUseCase.findAll()).thenReturn(List.of());

        var response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull().isEmpty();
    }

    @Test
    void findById_shouldReturnSign_whenFound() {
        UUID id = UUID.randomUUID();
        Sign sign = new Sign(
                id, "The Seeker", 2, "Early Spring", UUID.randomUUID(), "The Wanderer",
                "The seeker summary", "The seeker body", NOW, NOW);
        when(findSignByIdUseCase.findById(id)).thenReturn(sign);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        assertThat(response.getBody().name()).isEqualTo("The Seeker");
        assertThat(response.getBody().subArchetype()).isEqualTo("The Wanderer");
        verify(findSignByIdUseCase).findById(id);
    }

    @Test
    void findById_shouldReturnSignWithNullSubArchetype_whenRiftSignFound() {
        UUID id = UUID.randomUUID();
        Sign rift = new Sign(
                id, "The Rift", 1, "Deep Winter", UUID.randomUUID(), null,
                "The rift summary", "The rift body", NOW, NOW);
        when(findSignByIdUseCase.findById(id)).thenReturn(rift);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        assertThat(response.getBody().subArchetype()).isNull();
        verify(findSignByIdUseCase).findById(id);
    }
}
