package com.portfolio.erp.infrastructure.in.web.security;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import com.portfolio.erp.application.config.SecurityProperties;
import com.portfolio.erp.infrastructure.in.web.error.ProblemWriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Simple in-memory sliding-window rate limiter for the login endpoint.
 * Returns 429 (localized, problem+json) when the quota is exceeded.
 */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";

    private final SecurityProperties properties;
    private final ProblemWriter problemWriter;
    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    public LoginRateLimitFilter(SecurityProperties properties, ProblemWriter problemWriter) {
        this.properties = properties;
        this.problemWriter = problemWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || !LOGIN_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        long windowMillis = properties.loginRateLimit().window().toMillis();
        int maxAttempts = properties.loginRateLimit().maxAttempts();

        Deque<Long> timestamps = attempts.computeIfAbsent(clientKey, key -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > windowMillis) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= maxAttempts) {
                problemWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS, "error.rateLimit");
                return;
            }
            timestamps.addLast(now);
        }

        filterChain.doFilter(request, response);
    }
}
