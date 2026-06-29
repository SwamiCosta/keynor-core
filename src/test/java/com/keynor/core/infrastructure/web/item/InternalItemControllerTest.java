package com.keynor.core.infrastructure.web.item;

import com.keynor.core.application.dto.item.CreateItemRequest;
import com.keynor.core.application.dto.item.ItemResponse;
import com.keynor.core.application.dto.item.UpdateItemRequest;
import com.keynor.core.application.dto.shared.ChangeStatusRequest;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.item.*;
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
class InternalItemControllerTest {

    @Mock private CreateItemUseCase createItemUseCase;
    @Mock private UpdateItemUseCase updateItemUseCase;
    @Mock private ChangeItemStatusUseCase changeItemStatusUseCase;
    @Mock private DeleteItemUseCase deleteItemUseCase;
    @Mock private FindItemByIdUseCase findItemByIdUseCase;
    @Mock private FindAllItemsUseCase findAllItemsUseCase;
    @Mock private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private InternalItemController controller;

    private Item buildItem(UUID id) {
        Instant now = Instant.now();
        return new Item(id, "Shadowblade", "A cursed sword", "Body",
                List.of(), List.of(ItemCategory.WEAPON), EntityStatus.DRAFT, null, now, now);
    }

    @BeforeEach
    void setUp() {
        controller = new InternalItemController(
                createItemUseCase, updateItemUseCase, changeItemStatusUseCase,
                deleteItemUseCase, findItemByIdUseCase, findAllItemsUseCase,
                findLinkedEntitiesUseCase);
    }

    @Test
    void create_shouldReturn201AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(createItemUseCase.create(any())).thenReturn(buildItem(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateItemRequest("Shadowblade", "A cursed sword", "Body",
                List.of(), List.of("WEAPON"), "era-1", null, null, null);

        var response = controller.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo("Shadowblade");
        verify(createItemUseCase).create(any());
    }

    @Test
    void create_shouldPassCorrectCommandToUseCase() {
        UUID id = UUID.randomUUID();
        when(createItemUseCase.create(any())).thenReturn(buildItem(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateItemRequest("Shadowblade", null, null,
                List.of(), List.of("WEAPON"), "era-1", null, null, null);

        controller.create(request);

        ArgumentCaptor<CreateItemUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateItemUseCase.Command.class);
        verify(createItemUseCase).create(captor.capture());
        assertThat(captor.getValue().name()).isEqualTo("Shadowblade");
        assertThat(captor.getValue().categories()).containsExactly(ItemCategory.WEAPON);
    }

    @Test
    void create_shouldDefaultToDraftStatus_whenStatusIsNull() {
        UUID id = UUID.randomUUID();
        when(createItemUseCase.create(any())).thenReturn(buildItem(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateItemRequest("Shadowblade", null, null,
                List.of(), List.of("WEAPON"), "era-1", null, null, null);

        controller.create(request);

        ArgumentCaptor<CreateItemUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateItemUseCase.Command.class);
        verify(createItemUseCase).create(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EntityStatus.DRAFT);
    }

    @Test
    void create_shouldPassCanonStatus_whenStatusIsCanon() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        Item canonItem = new Item(id, "Shadowblade", null, null, List.of(),
                List.of(ItemCategory.WEAPON), EntityStatus.CANON, null, now, now);
        when(createItemUseCase.create(any())).thenReturn(canonItem);
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new CreateItemRequest("Shadowblade", null, null,
                List.of(), List.of("WEAPON"), "era-1", null, "CANON", null);

        controller.create(request);

        ArgumentCaptor<CreateItemUseCase.Command> captor =
                ArgumentCaptor.forClass(CreateItemUseCase.Command.class);
        verify(createItemUseCase).create(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EntityStatus.CANON);
    }

    @Test
    void create_shouldThrowIllegalArgumentException_whenStatusIsDeprecated() {
        var request = new CreateItemRequest("Shadowblade", null, null,
                List.of(), List.of("WEAPON"), "era-1", null, "DEPRECATED", null);

        assertThatThrownBy(() -> controller.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEPRECATED");
    }

    @Test
    void update_shouldReturn200AndResponseBody_whenCommandIsValid() {
        UUID id = UUID.randomUUID();
        when(updateItemUseCase.update(eq(id), any())).thenReturn(buildItem(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var request = new UpdateItemRequest("Shadowblade Updated", null, null,
                List.of(), List.of("WEAPON"), "era-1", null, null);

        var response = controller.update(id, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(updateItemUseCase).update(eq(id), any());
    }

    @Test
    void changeStatus_shouldReturn200AndCallUseCase() {
        UUID id = UUID.randomUUID();
        when(changeItemStatusUseCase.changeStatus(id, EntityStatus.CANON)).thenReturn(buildItem(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.changeStatus(id, new ChangeStatusRequest("CANON"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(changeItemStatusUseCase).changeStatus(id, EntityStatus.CANON);
    }

    @Test
    void delete_shouldReturn204AndCallUseCase() {
        UUID id = UUID.randomUUID();

        var response = controller.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteItemUseCase).delete(id);
    }

    @Test
    void findAll_shouldPassPaginationAndReturnMappedResponse() {
        UUID id = UUID.randomUUID();
        when(findAllItemsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(buildItem(id)), 0, 20, 1));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<ItemResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllItemsUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(0);
        assertThat(pageCaptor.getValue().size()).isEqualTo(20);
    }

    @Test
    void findAll_shouldNotApplyCanonFilter_whenNoStatusProvided() {
        when(findAllItemsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllItemsUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).isEmpty();
    }

    @Test
    void findById_shouldDelegateAndMapResult() {
        UUID id = UUID.randomUUID();
        when(findItemByIdUseCase.findById(id)).thenReturn(buildItem(id));
        when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findItemByIdUseCase).findById(id);
    }
}
