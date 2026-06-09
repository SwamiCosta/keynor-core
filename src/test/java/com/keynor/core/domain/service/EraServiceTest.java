package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.port.out.EraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EraServiceTest {

    @Mock
    private EraRepository eraRepository;

    private EraService eraService;

    @BeforeEach
    void setUp() {
        eraService = new EraService(eraRepository);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        Era era = new Era("era-1", "The First Age", 1, "Year 1–500", "An age of beginnings",
                MapType.NAVIGABLE, "map-1", "#FF0000");
        when(eraRepository.findAll()).thenReturn(List.of(era));

        List<Era> result = eraService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("era-1");
        verify(eraRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoErasExist() {
        when(eraRepository.findAll()).thenReturn(List.of());

        List<Era> result = eraService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnEra_whenEraExists() {
        Era era = new Era("era-1", "The First Age", 1, "Year 1–500", "An age of beginnings",
                MapType.NAVIGABLE, "map-1", "#FF0000");
        when(eraRepository.findById("era-1")).thenReturn(Optional.of(era));

        Era result = eraService.findById("era-1");

        assertThat(result.getId()).isEqualTo("era-1");
        assertThat(result.getName()).isEqualTo("The First Age");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenEraDoesNotExist() {
        when(eraRepository.findById("unknown-era")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eraService.findById("unknown-era"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
