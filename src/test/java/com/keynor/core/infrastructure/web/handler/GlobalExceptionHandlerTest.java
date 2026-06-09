package com.keynor.core.infrastructure.web.handler;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.keynor.core.domain.exception.DuplicateEntityNameException;
import com.keynor.core.domain.exception.EntityNotFoundException;
import com.keynor.core.domain.exception.InvalidStatusTransitionException;
import com.keynor.core.domain.model.shared.EntityStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private ListAppender<ILoggingEvent> logAppender;
    private Logger handlerLogger;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();

        handlerLogger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        handlerLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        handlerLogger.detachAppender(logAppender);
        logAppender.stop();
    }

    @Test
    void handleEntityNotFound_shouldReturnNotFoundStatus() {
        EntityNotFoundException ex = new EntityNotFoundException("Character", UUID.randomUUID());

        ProblemDetail result = handler.handleEntityNotFound(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void handleEntityNotFound_shouldLogAtWarnLevel() {
        EntityNotFoundException ex = new EntityNotFoundException("Character", UUID.randomUUID());

        handler.handleEntityNotFound(ex);

        assertThat(logAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
    }

    @Test
    void handleDuplicateName_shouldReturnConflictStatus() {
        DuplicateEntityNameException ex = new DuplicateEntityNameException("Character", "Araveth");

        ProblemDetail result = handler.handleDuplicateName(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    void handleDuplicateName_shouldLogAtWarnLevel() {
        DuplicateEntityNameException ex = new DuplicateEntityNameException("Character", "Araveth");

        handler.handleDuplicateName(ex);

        assertThat(logAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
    }

    @Test
    void handleInvalidTransition_shouldReturnUnprocessableEntityStatus() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException(
                EntityStatus.DEPRECATED, EntityStatus.CANON);

        ProblemDetail result = handler.handleInvalidTransition(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    @Test
    void handleInvalidTransition_shouldLogAtWarnLevel() {
        InvalidStatusTransitionException ex = new InvalidStatusTransitionException(
                EntityStatus.DEPRECATED, EntityStatus.CANON);

        handler.handleInvalidTransition(ex);

        assertThat(logAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequestStatus() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        ProblemDetail result = handler.handleIllegalArgument(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void handleIllegalArgument_shouldLogAtWarnLevel() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");

        handler.handleIllegalArgument(ex);

        assertThat(logAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
    }

    @Test
    void handleValidation_shouldReturnBadRequestStatus() {
        MethodArgumentNotValidException ex = buildValidationException("name", "must not be blank");

        ProblemDetail result = handler.handleValidation(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void handleValidation_shouldLogAtWarnLevel() {
        MethodArgumentNotValidException ex = buildValidationException("name", "must not be blank");

        handler.handleValidation(ex);

        assertThat(logAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
    }

    @Test
    void handleUnexpected_shouldReturnInternalServerErrorStatus() {
        Exception ex = new RuntimeException("Unexpected failure");

        ProblemDetail result = handler.handleUnexpected(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void handleUnexpected_shouldLogAtErrorLevel() {
        Exception ex = new RuntimeException("Unexpected failure");

        handler.handleUnexpected(ex);

        assertThat(logAppender.list)
                .hasSize(1)
                .first()
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.ERROR));
    }

    @Test
    void handleUnexpected_shouldNotExposeInternalDetailsToClient() {
        Exception ex = new RuntimeException("Internal DB connection refused at 192.168.1.1:5432");

        ProblemDetail result = handler.handleUnexpected(ex);

        assertThat(result.getDetail()).doesNotContain("192.168.1.1");
    }

    private MethodArgumentNotValidException buildValidationException(String field, String message) {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", field, message);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        MethodParameter methodParameter = new MethodParameter(
                GlobalExceptionHandlerTest.class.getDeclaredMethods()[0], -1);
        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }
}
