package com.keynor.core.infrastructure.web.lore;

import com.keynor.core.application.dto.lore.LoreResponse;
import com.keynor.core.application.dto.shared.PagedResponse;
import com.keynor.core.domain.model.lore.Lore;
import com.keynor.core.domain.model.lore.LoreCategory;
import com.keynor.core.domain.model.shared.EntityFilter;
import com.keynor.core.domain.model.shared.EntityStatus;
import com.keynor.core.domain.model.shared.PageRequest;
import com.keynor.core.domain.model.shared.PageResult;
import com.keynor.core.domain.port.in.lore.FindAllLoreUseCase;
import com.keynor.core.domain.port.in.lore.FindLoreByIdUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicLoreControllerTest {

    @Mock
    private FindAllLoreUseCase findAllLoreUseCase;

    @Mock
    private FindLoreByIdUseCase findLoreByIdUseCase;

    @Mock
    private FindLinkedEntitiesUseCase findLinkedEntitiesUseCase;

    private PublicLoreController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicLoreController(findAllLoreUseCase, findLoreByIdUseCase, findLinkedEntitiesUseCase);
        org.mockito.Mockito.lenient().when(findLinkedEntitiesUseCase.findLinks(any(), any())).thenReturn(List.of());
    }

    @Test
    void findAll_shouldAlwaysFilterByCanonStatus() {
        when(findAllLoreUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 0, 20, 0));

        controller.findAll(null, 0, 20);

        ArgumentCaptor<EntityFilter> filterCaptor = ArgumentCaptor.forClass(EntityFilter.class);
        verify(findAllLoreUseCase).findAll(filterCaptor.capture(), any());
        assertThat(filterCaptor.getValue().statuses()).containsExactly(EntityStatus.CANON);
    }

    @Test
    void findAll_shouldPassPaginationParamsToUseCase() {
        when(findAllLoreUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(), 3, 15, 0));

        controller.findAll(null, 3, 15);

        ArgumentCaptor<PageRequest> pageCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(findAllLoreUseCase).findAll(any(), pageCaptor.capture());
        assertThat(pageCaptor.getValue().page()).isEqualTo(3);
        assertThat(pageCaptor.getValue().size()).isEqualTo(15);
    }

    @Test
    void findAll_shouldReturnMappedPagedResponse() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Lore lore = new Lore(id, "The Great Myth", "A summary", "Body text",
                List.of(), List.of(LoreCategory.MYTH),
                EntityStatus.CANON, null, now, now);
        when(findAllLoreUseCase.findAll(any(), any()))
                .thenReturn(new PageResult<>(List.of(lore), 0, 20, 1));

        var response = controller.findAll(null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PagedResponse<LoreResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.content()).hasSize(1);
        assertThat(body.content().get(0).id()).isEqualTo(id);
        assertThat(body.content().get(0).name()).isEqualTo("The Great Myth");
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();
        Lore lore = new Lore(id, "The Great Myth", "A summary", "Body text",
                List.of(), List.of(LoreCategory.HISTORY),
                EntityStatus.CANON, null, now, now);
        when(findLoreByIdUseCase.findById(id)).thenReturn(lore);

        var response = controller.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(id);
        verify(findLoreByIdUseCase).findById(id);
    }
}
