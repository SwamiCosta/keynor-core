package com.keynor.core.infrastructure.web.handler;

import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.model.shared.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleEntityNotFound_shouldReturn404_whenEntityNotFoundExceptionThrown() {
        UUID id = UUID.randomUUID();
        EntityNotFoundException ex = new EntityNotFoundException("Character", id);

        ProblemDetail result = handler.handleEntityNotFound(ex);

        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getDetail()).contains("Character");
        assertThat(result.getDetail()).contains(id.toString());
    }

    @Test
    void handleDuplicateName_shouldReturn409_whenDuplicateEntityNameExceptionThrown() {
        DuplicateEntityNameException ex = new DuplicateEntityNameException("Place", "Thornvale");

        ProblemDetail result = handler.handleDuplicateName(ex);

        assertThat(result.getStatus()).isEqualTo(409);
        assertThat(result.getDetail()).contains("Place");
        assertThat(result.getDetail()).contains("Thornvale");
    }

    @Test
    void handleInvalidTransition_shouldReturn422_whenInvalidStatusTransitionExceptionThrown() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException(
                EntityStatus.DEPRECATED, EntityStatus.CANON);

        ProblemDetail result = handler.handleInvalidTransition(ex);

        assertThat(result.getStatus()).isEqualTo(422);
        assertThat(result.getDetail()).contains("DEPRECATED");
        assertThat(result.getDetail()).contains("CANON");
    }

    @Test
    void handleIllegalArgument_shouldReturn400_whenIllegalArgumentExceptionThrown() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid category value: UNKNOWN");

        ProblemDetail result = handler.handleIllegalArgument(ex);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).contains("Invalid category value: UNKNOWN");
    }

    @Test
    void handleValidation_shouldReturn400WithFieldDetail_whenMethodArgumentNotValidExceptionThrown() {
        FieldError fieldError = new FieldError("createCharacterRequest", "name", "must not be blank");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ProblemDetail result = handler.handleValidation(methodArgumentNotValidException);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).contains("name");
        assertThat(result.getDetail()).contains("must not be blank");
    }

    @Test
    void handleValidation_shouldCombineMultipleFieldErrors_whenSeveralFieldsFail() {
        FieldError nameError = new FieldError("createCharacterRequest", "name", "must not be blank");
        FieldError categoryError = new FieldError("createCharacterRequest", "categories", "must not be null");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(nameError, categoryError));

        ProblemDetail result = handler.handleValidation(methodArgumentNotValidException);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).contains("name");
        assertThat(result.getDetail()).contains("categories");
    }

    @Test
    void handleValidation_shouldReturnFallbackMessage_whenNoFieldErrors() {
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of());

        ProblemDetail result = handler.handleValidation(methodArgumentNotValidException);

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo("Validation failed");
    }
}
