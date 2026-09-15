package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int LOGIN_LIMIT = 5;
    private static final int REGISTER_LIMIT = 3;

    private final ConcurrentHashMap<String, FixedWindow> windows = new ConcurrentHashMap<>();

    private record FixedWindow(Instant start, AtomicInteger count) {}

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        Integer limit = null;
        if (path.equals("/api/auth/login")) {
            limit = LOGIN_LIMIT;
        } else if (path.equals("/api/auth/register")) {
            limit = REGISTER_LIMIT;
        }

        if (limit != null) {
            String key = path + ":" + getClientIP(request);
            FixedWindow window = windows.compute(key, (k, existing) -> {
                if (existing == null || existing.start().plus(WINDOW).isBefore(Instant.now())) {
                    return new FixedWindow(Instant.now(), new AtomicInteger(1));
                }
                existing.count().incrementAndGet();
                return existing;
            });

            if (window.count().get() > limit) {
                long retrySeconds = Duration.between(Instant.now(), window.start().plus(WINDOW)).getSeconds();
                response.setStatus(429);
                response.setHeader("Retry-After", String.valueOf(Math.max(retrySeconds, 1)));
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"error\":\"Demasiadas peticiones. Intente más tarde.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIP(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    public void reset() {
        windows.clear();
    }
}