package com.portfolio.erp.infrastructure.in.web.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes RFC 7807 problem+json responses outside the MVC pipeline (security
 * filters), keeping the same shape as {@link GlobalExceptionHandler}.
 */
@Component
public class ProblemWriter {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public ProblemWriter(ObjectMapper objectMapper, MessageSource messageSource) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, String code, Object... args) throws IOException {
        Locale locale = request.getLocale() != null ? request.getLocale() : Locale.ENGLISH;
        String detail = messageSource.getMessage(code, args, code, locale);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("type", "about:blank");
        body.put("title", status.getReasonPhrase());
        body.put("status", status.value());
        body.put("detail", detail);
        body.put("instance", request.getRequestURI());
        body.put("code", code);
        body.put("timestamp", Instant.now().toString());

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
