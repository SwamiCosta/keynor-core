package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.sign.Sign;
import com.keynor.core.domain.port.out.SignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignServiceTest {

    @Mock
    private SignRepository signRepository;

    private SignService signService;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        signService = new SignService(signRepository);
    }

    @Test
    void findAll_shouldDelegateToFindAllOrderedBySignOrderAndPreserveOrder() {
        Sign first = new Sign(
                UUID.randomUUID(), "The Rift", 1, "Deep Winter", UUID.randomUUID(), null,
                "The rift summary", "The rift body", NOW, NOW);
        Sign second = new Sign(
                UUID.randomUUID(), "The Seeker", 2, "Early Spring", UUID.randomUUID(), "The Wanderer",
                "The seeker summary", "The seeker body", NOW, NOW);

        when(signRepository.findAllOrderedBySignOrder()).thenReturn(List.of(first, second));

        List<Sign> result = signService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("The Rift");
        assertThat(result.get(0).getSignOrder()).isEqualTo(1);
        assertThat(result.get(1).getName()).isEqualTo("The Seeker");
        assertThat(result.get(1).getSignOrder()).isEqualTo(2);
        verify(signRepository).findAllOrderedBySignOrder();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntriesExist() {
        when(signRepository.findAllOrderedBySignOrder()).thenReturn(List.of());

        List<Sign> result = signService.findAll();

        assertThat(result).isEmpty();
        verify(signRepository).findAllOrderedBySignOrder();
    }

    @Test
    void findById_shouldReturnSign_whenFound() {
        UUID id = UUID.randomUUID();
        Sign sign = new Sign(
                id, "The Seeker", 2, "Early Spring", UUID.randomUUID(), "The Wanderer",
                "The seeker summary", "The seeker body", NOW, NOW);
        when(signRepository.findById(id)).thenReturn(Optional.of(sign));

        Sign result = signService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("The Seeker");
        assertThat(result.getSubArchetype()).isEqualTo("The Wanderer");
    }

    @Test
    void findById_shouldReturnSignWithNullSubArchetype_whenRiftSignFound() {
        UUID id = UUID.randomUUID();
        Sign rift = new Sign(
                id, "The Rift", 1, "Deep Winter", UUID.randomUUID(), null,
                "The rift summary", "The rift body", NOW, NOW);
        when(signRepository.findById(id)).thenReturn(Optional.of(rift));

        Sign result = signService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("The Rift");
        assertThat(result.getSubArchetype()).isNull();
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(signRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> signService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
