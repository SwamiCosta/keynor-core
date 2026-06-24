package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.model.archetype.Archetype;
import com.keynor.core.domain.port.out.ArchetypeRepository;
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
class ArchetypeServiceTest {

    @Mock
    private ArchetypeRepository archetypeRepository;

    private ArchetypeService archetypeService;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        archetypeService = new ArchetypeService(archetypeRepository);
    }

    @Test
    void findAll_shouldDelegateToRepositoryAndReturnListAsIs() {
        Archetype obsession = new Archetype(
                UUID.randomUUID(), "Obsession", null, null, null, null, null, null, "The obsessive archetype", NOW, NOW);
        Archetype theSeeker = new Archetype(
                UUID.randomUUID(), "The Seeker", "Air", "Wands", "Explorer", "Choleric", "Intuition", "Outward",
                "The seeking archetype", NOW, NOW);

        when(archetypeRepository.findAll()).thenReturn(List.of(obsession, theSeeker));

        List<Archetype> result = archetypeService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Obsession");
        assertThat(result.get(0).getElement()).isNull();
        assertThat(result.get(0).getSuit()).isNull();
        assertThat(result.get(0).getVocation()).isNull();
        assertThat(result.get(0).getTemperament()).isNull();
        assertThat(result.get(0).getCognitiveFunction()).isNull();
        assertThat(result.get(0).getSelfRelation()).isNull();
        assertThat(result.get(1).getName()).isEqualTo("The Seeker");
        assertThat(result.get(1).getElement()).isEqualTo("Air");
        assertThat(result.get(1).getSuit()).isEqualTo("Wands");
        assertThat(result.get(1).getVocation()).isEqualTo("Explorer");
        assertThat(result.get(1).getTemperament()).isEqualTo("Choleric");
        assertThat(result.get(1).getCognitiveFunction()).isEqualTo("Intuition");
        assertThat(result.get(1).getSelfRelation()).isEqualTo("Outward");
        verify(archetypeRepository).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoEntriesExist() {
        when(archetypeRepository.findAll()).thenReturn(List.of());

        List<Archetype> result = archetypeService.findAll();

        assertThat(result).isEmpty();
        verify(archetypeRepository).findAll();
    }

    @Test
    void findById_shouldReturnArchetype_whenFound() {
        UUID id = UUID.randomUUID();
        Archetype archetype = new Archetype(
                id, "The Seeker", "Air", "Wands", "Explorer", "Choleric", "Intuition", "Outward",
                "The seeking archetype", NOW, NOW);
        when(archetypeRepository.findById(id)).thenReturn(Optional.of(archetype));

        Archetype result = archetypeService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("The Seeker");
        assertThat(result.getElement()).isEqualTo("Air");
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(archetypeRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> archetypeService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
