package com.keynor.core.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private FilterChain filterChain;

    private RequestLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RequestLoggingFilter();
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void doFilter_shouldPopulateTraceIdInMDC_duringChainExecution() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/characters");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> capturedTraceId = new AtomicReference<>();

        doAnswer(invocation -> {
            capturedTraceId.set(MDC.get("traceId"));
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilter(request, response, filterChain);

        assertThat(capturedTraceId.get())
                .as("traceId must be present in MDC during chain execution")
                .isNotNull()
                .isNotBlank();
    }

    @Test
    void doFilter_shouldGenerateUniqueTraceIdPerRequest() throws IOException, ServletException {
        MockHttpServletRequest firstRequest = new MockHttpServletRequest("GET", "/api/v1/characters");
        MockHttpServletRequest secondRequest = new MockHttpServletRequest("GET", "/api/v1/places");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> firstTraceId = new AtomicReference<>();
        AtomicReference<String> secondTraceId = new AtomicReference<>();

        doAnswer(invocation -> {
            firstTraceId.set(MDC.get("traceId"));
            return null;
        }).when(filterChain).doFilter(any(), any());
        filter.doFilter(firstRequest, response, filterChain);

        doAnswer(invocation -> {
            secondTraceId.set(MDC.get("traceId"));
            return null;
        }).when(filterChain).doFilter(any(), any());
        filter.doFilter(secondRequest, response, filterChain);

        assertThat(firstTraceId.get()).isNotEqualTo(secondTraceId.get());
    }

    @Test
    void doFilter_shouldClearMDCAfterChainCompletes() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/characters");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(MDC.get("traceId"))
                .as("traceId must be cleared from MDC after the filter chain completes")
                .isNull();
    }

    @Test
    void doFilter_shouldClearMDCEvenWhenChainThrowsException() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/characters");
        MockHttpServletResponse response = new MockHttpServletResponse();

        doAnswer(invocation -> {
            throw new ServletException("Simulated downstream failure");
        }).when(filterChain).doFilter(any(), any());

        try {
            filter.doFilter(request, response, filterChain);
        } catch (ServletException ignored) {
            // expected
        }

        assertThat(MDC.get("traceId"))
                .as("traceId must be cleared from MDC even when the chain throws an exception")
                .isNull();
    }

    @Test
    void doFilter_shouldDelegateToFilterChain() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/v1/characters/some-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
