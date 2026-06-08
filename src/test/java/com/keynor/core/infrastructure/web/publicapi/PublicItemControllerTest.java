package com.keynor.core.infrastructure.web.publicapi;

import com.keynor.core.application.dto.item.ItemResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.item.Item;
import com.keynor.core.domain.model.item.ItemCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.item.FindAllItemsUseCase;
import com.keynor.core.domain.port.in.item.FindItemByIdUseCase;
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
class PublicItemControllerTest {

    @Mock
    private FindAllItemsUseCase findAllItemsUseCase;

    @Mock
    private FindItemByIdUseCase findItemByIdUseCase;

    private PublicItemController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicItemController(findAllItemsUseCase, findItemByIdUseCase);
    }

    @Test
    void findAll_shouldAlwaysFilterByCanonStatus() {
        when(findAllItemsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllItemsUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).containsExactly(EntityStatus.CANON);
    }

    @Test
    void findAll_shouldPassPaginationParamsToUseCase() {
        when(findAllItemsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 3, 10, 0));

        controller.findAll(null, null, 3, 10);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllItemsUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(3);
        assertThat(pageCaptor.getValue().size()).isEqualTo(10);
    }

    @Test
    void findAll_shouldReturnMappedPagedResponse() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Item item = new Item(id, "Sword of Dawn", "A legendary blade", "Body", List.of("sword"),
                List.of(), List.of(ItemCategory.WEAPON), EntityStatus.CANON, null, now, now);
        when(findAllItemsUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(item), 0, 20, 1));

        var response = controller.findAll(null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<ItemResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.content()).hasSize(1);
        assertThat(body.content().get(0).id()).isEqualTo(id);
        assertThat(body.content().get(0).name()).isEqualTo("Sword of Dawn");
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Item item = new Item(id, "Sword of Dawn", "A legendary blade", "Body", List.of(),
                List.of(), List.of(ItemCategory.ARTIFACT), EntityStatus.CANON, null, now, now);
        when(findItemByIdUseCase.findById(id)).thenReturn(item);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findItemByIdUseCase).findById(id);
    }
}
