package com.keynor.core.domain.service;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.exception.UnknownEraNameException;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.Language;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.model.shared.Timeline;
import com.keynor.core.domain.port.in.item.CreateItemUseCase;
import com.keynor.core.domain.port.in.item.UpdateItemUseCase;
import com.keynor.core.domain.port.in.shared.CreateHiddenContentLockUseCase;
import com.keynor.core.domain.port.out.EntityLinkRepository;
import com.keynor.core.domain.port.out.EraRepository;
import com.keynor.core.domain.port.out.ItemRepository;
import com.keynor.core.domain.port.out.UniverseEntityLookupRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private EntityLinkRepository entityLinkRepository;

    @Mock
    private EraRepository eraRepository;

    @Mock
    private UniverseEntityLookupRepository universeEntityLookupRepository;

    @Mock
    private CreateHiddenContentLockUseCase createHiddenContentLockUseCase;

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(itemRepository, entityLinkRepository, eraRepository,
                universeEntityLookupRepository, createHiddenContentLockUseCase);
    }

    @Test
    void create_shouldReturnSavedItem_whenNameIsUnique() {
        var command = new CreateItemUseCase.Command(
                "Shadowblade", "A cursed sword", "Long description...",
                List.of(),
                List.of(ItemCategory.WEAPON),
                null,
                null, Language.EN, null, null,
                null, false, null, null, false);
        when(itemRepository.existsByNameAndLanguage("Shadowblade", Language.EN)).thenReturn(false);
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.create(command);

        assertThat(result.getName()).isEqualTo("Shadowblade");
        assertThat(result.getStatus()).isEqualTo(EntityStatus.DRAFT);
        assertThat(result.getCategories()).containsExactly(ItemCategory.WEAPON);
        verify(itemRepository).save(any());
    }

    @Test
    void create_shouldReturnSavedItemWithCanonStatus_whenStatusIsCanon() {
        var command = new CreateItemUseCase.Command(
                "Shadowblade", "A cursed sword", "Long description...",
                List.of(),
                List.of(ItemCategory.WEAPON),
                null,
                EntityStatus.CANON, Language.EN, null, null,
                null, false, null, null, false);
        when(itemRepository.existsByNameAndLanguage("Shadowblade", Language.EN)).thenReturn(false);
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.create(command);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
        verify(itemRepository).save(any());
    }

    @Test
    void create_shouldThrowDuplicateEntityNameException_whenNameAlreadyExists() {
        var command = new CreateItemUseCase.Command(
                "Shadowblade", null, null, List.of(),
                List.of(ItemCategory.ARTIFACT), null, null, Language.EN, null, null, null, false, null, null, false);
        when(itemRepository.existsByNameAndLanguage("Shadowblade", Language.EN)).thenReturn(true);

        assertThatThrownBy(() -> itemService.create(command))
                .isInstanceOf(DuplicateEntityNameException.class)
                .hasMessageContaining("Shadowblade");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowUnknownEraNameException_whenTimelineEraDoesNotExist() {
        var command = new CreateItemUseCase.Command(
                "Shadowblade", null, null, List.of(), List.of(ItemCategory.WEAPON),
                new Timeline("Nonexistent Era", null), null, Language.EN, null, null, null, false, null, null, false);
        when(itemRepository.existsByNameAndLanguage("Shadowblade", Language.EN)).thenReturn(false);
        when(eraRepository.findByName("Nonexistent Era")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.create(command))
                .isInstanceOf(UnknownEraNameException.class)
                .hasMessageContaining("Nonexistent Era");
        verify(itemRepository, never()).save(any());
    }

    @Test
    void findById_shouldReturnItem_whenItemExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Item item = new Item(id, "Shadowblade", null, null, List.of(),
                List.of(ItemCategory.WEAPON), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        Item result = itemService.findById(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenItemDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(itemRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.findById(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAll_shouldDelegateToRepository() {
        EntityFilter filter = new EntityFilter(Language.EN, List.of(), List.of(), false, false);
        PageRequest pageRequest = new PageRequest(0, 10);
        when(itemRepository.findAll(filter, pageRequest))
                .thenReturn(new PageResult<>(List.of(), 0, 10, 0));

        PageResult<Item> result = itemService.findAll(filter, pageRequest);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void changeStatus_shouldTransitionFromDraftToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Item item = new Item(id, "Shadowblade", null, null, List.of(),
                List.of(ItemCategory.WEAPON), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Item result = itemService.changeStatus(id, EntityStatus.CANON);

        assertThat(result.getStatus()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void changeStatus_shouldThrowInvalidStatusTransitionException_whenDeprecatedToCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Item item = new Item(id, "Shadowblade", null, null, List.of(),
                List.of(ItemCategory.WEAPON), EntityStatus.DEPRECATED, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.changeStatus(id, EntityStatus.CANON))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void delete_shouldCallRepository_whenItemExists() {
        UUID id = UUID.randomUUID();
        when(itemRepository.existsById(id)).thenReturn(true);

        itemService.delete(id);

        verify(itemRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrowEntityNotFoundException_whenItemDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(itemRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> itemService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void update_shouldReplaceFields_whenItemExists() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Item item = new Item(id, "Old Name", null, null, List.of(),
                List.of(ItemCategory.TOOL), EntityStatus.DRAFT, null, now, now, Language.EN, UUID.randomUUID(), UUID.randomUUID(), false, false);
        when(itemRepository.findById(id)).thenReturn(Optional.of(item));
        when(itemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = new UpdateItemUseCase.Command(
                "New Name", "New summary", "New body",
                List.of(),
                List.of(ItemCategory.WEAPON, ItemCategory.ARTIFACT), null, null, false, null, null, false);

        Item result = itemService.update(id, command);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getCategories()).containsExactlyInAnyOrder(ItemCategory.WEAPON, ItemCategory.ARTIFACT);
    }
}
