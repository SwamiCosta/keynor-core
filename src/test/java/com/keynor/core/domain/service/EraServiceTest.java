package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.era.EraImportance;
import com.keynor.core.domain.model.era.EraType;
import com.keynor.core.domain.port.out.EraRepository;
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
class EraServiceTest {

    @Mock
    private EraRepository eraRepository;

    private EraService eraService;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        eraService = new EraService(eraRepository);
    }

    @Test
    void findAll_shouldDelegateToRepositoryAndReturnOrderedList() {
        Era eraInterval = new Era(
                UUID.randomUUID(), "Age of Creation", 1, EraType.ERA, null, "The first age", NOW, NOW);
        Era temporalPoint = new Era(
                UUID.randomUUID(), "The Great Sundering", 2, EraType.POINT, EraImportance.MAJOR, "Cataclysmic event", NOW, NOW);
        Era eraInterval2 = new Era(
                UUID.randomUUID(), "Age of Silence", 3, EraType.ERA, null, "Age of quiet", NOW, NOW);

        when(eraRepository.findAllOrderedByIndex()).thenReturn(List.of(eraInterval, temporalPoint, eraInterval2));

        List<Era> result = eraService.findAll();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getType()).isEqualTo(EraType.ERA);
        assertThat(result.get(0).getImportance()).isNull();
        assertThat(result.get(1).getType()).isEqualTo(EraType.POINT);
        assertThat(result.get(1).getImportance()).isEqualTo(EraImportance.MAJOR);
        assertThat(result.get(2).getType()).isEqualTo(EraType.ERA);
        verify(eraRepository).findAllOrderedByIndex();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntriesExist() {
        when(eraRepository.findAllOrderedByIndex()).thenReturn(List.of());

        List<Era> result = eraService.findAll();

        assertThat(result).isEmpty();
        verify(eraRepository).findAllOrderedByIndex();
    }

    @Test
    void findById_shouldReturnEra_whenFound() {
        UUID id = UUID.randomUUID();
        Era era = new Era(id, "Age of Creation", 1, EraType.ERA, null, "The first age", NOW, NOW);
        when(eraRepository.findById(id)).thenReturn(Optional.of(era));

        Era result = eraService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Age of Creation");
        assertThat(result.getType()).isEqualTo(EraType.ERA);
        assertThat(result.getImportance()).isNull();
    }

    @Test
    void findById_shouldReturnTemporalPoint_whenFound() {
        UUID id = UUID.randomUUID();
        Era point = new Era(id, "The Great Sundering", 2, EraType.POINT, EraImportance.MAJOR, "Cataclysmic event", NOW, NOW);
        when(eraRepository.findById(id)).thenReturn(Optional.of(point));

        Era result = eraService.findById(id);

        assertThat(result.getType()).isEqualTo(EraType.POINT);
        assertThat(result.getImportance()).isEqualTo(EraImportance.MAJOR);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(eraRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eraService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- Domain invariant tests ---

    @Test
    void era_constructor_shouldThrow_whenPointHasNullImportance() {
        assertThatThrownBy(() ->
                new Era(UUID.randomUUID(), "Some Point", 1, EraType.POINT, null, "desc", NOW, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("POINT");
    }

    @Test
    void era_constructor_shouldThrow_whenEraHasNonNullImportance() {
        assertThatThrownBy(() ->
                new Era(UUID.randomUUID(), "Some Era", 1, EraType.ERA, EraImportance.STANDARD, "desc", NOW, NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ERA");
    }

    @Test
    void era_constructor_shouldAccept_pointWithStandardImportance() {
        Era point = new Era(UUID.randomUUID(), "Minor Event", 1, EraType.POINT, EraImportance.STANDARD, "desc", NOW, NOW);

        assertThat(point.getType()).isEqualTo(EraType.POINT);
        assertThat(point.getImportance()).isEqualTo(EraImportance.STANDARD);
    }

    @Test
    void era_constructor_shouldAccept_eraWithNullImportance() {
        Era era = new Era(UUID.randomUUID(), "Silent Age", 1, EraType.ERA, null, "desc", NOW, NOW);

        assertThat(era.getType()).isEqualTo(EraType.ERA);
        assertThat(era.getImportance()).isNull();
    }
}
