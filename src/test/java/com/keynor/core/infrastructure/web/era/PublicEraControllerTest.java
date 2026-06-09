package com.keynor.core.infrastructure.web.era;

import com.keynor.core.application.dto.era.EraResponse;
import com.keynor.core.domain.model.era.Era;
import com.keynor.core.domain.model.place.MapType;
import com.keynor.core.domain.port.in.era.FindAllErasUseCase;
import com.keynor.core.domain.port.in.era.FindEraByIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicEraControllerTest {

    @Mock
    private FindAllErasUseCase findAllErasUseCase;

    @Mock
    private FindEraByIdUseCase findEraByIdUseCase;

    private PublicEraController controller;

    @BeforeEach
    void setUp() {
        controller = new PublicEraController(findAllErasUseCase, findEraByIdUseCase);
    }

    @Test
    void findAll_shouldDelegateToUseCaseAndMapResult() {
        Era era = new Era("era-1", "The First Age", 1, "Year 1–500", "An age of beginnings",
                MapType.NAVIGABLE, "map-1", "#FF0000");
        when(findAllErasUseCase.findAll()).thenReturn(List.of(era));

        var response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<EraResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSize(1);
        assertThat(body.get(0).id()).isEqualTo("era-1");
        assertThat(body.get(0).name()).isEqualTo("The First Age");
        verify(findAllErasUseCase).findAll();
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoErasExist() {
        when(findAllErasUseCase.findAll()).thenReturn(List.of());

        var response = controller.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void findById_shouldDelegateToUseCaseAndMapResult() {
        Era era = new Era("era-1", "The First Age", 1, "Year 1–500", "An age of beginnings",
                MapType.NAVIGABLE, "map-1", "#FF0000");
        when(findEraByIdUseCase.findById("era-1")).thenReturn(era);

        var response = controller.findById("era-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("era-1");
        verify(findEraByIdUseCase).findById("era-1");
    }

    @Test
    void findById_shouldPassStringIdDirectlyToUseCase() {
        Era era = new Era("era-primordial", "The Primordial Void", 0, "Before time", "The age before creation",
                MapType.ABSTRACT, null, "#000000");
        when(findEraByIdUseCase.findById("era-primordial")).thenReturn(era);

        var response = controller.findById("era-primordial");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().id()).isEqualTo("era-primordial");
        verify(findEraByIdUseCase).findById("era-primordial");
    }
}
