package com.keynor.core.infrastructure.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);

        long startTime = System.currentTimeMillis();
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String remoteAddress = httpRequest.getRemoteAddr();

        log.info("→ {} {} from {}", method, uri, remoteAddress);

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("← {} {} {}ms", method, uri, elapsed);
            MDC.clear();
        }
    }
}
