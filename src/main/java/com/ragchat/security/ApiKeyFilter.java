package com.ragchat.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ragchat.dto.ApiResponse;
import com.ragchat.exception.RateLimitException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ApiKeyFilter implements Filter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // Paths excluded from auth (health + swagger)
    private static final String[] PUBLIC_PATHS = {
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };

    @Value("${app.api-key}")
    private String validApiKey;

    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Per-IP rate limiter buckets
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String path = request.getRequestURI();

        // Skip auth for public paths
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // API key validation
        String apiKey = request.getHeader(API_KEY_HEADER);
        if (apiKey == null || !apiKey.equals(validApiKey)) {
            log.warn("Rejected request with invalid API key from IP: {}", getClientIp(request));
            writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or missing API key");
            return;
        }

        // Rate limiting per IP
        String clientIp = getClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);
        if (!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            writeError(response, HttpStatus.TOO_MANY_REQUESTS,
                    "Rate limit exceeded. Max " + requestsPerMinute + " requests/minute.");
            return;
        }

        chain.doFilter(request, response);
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.classic(
                requestsPerMinute,
                Refill.greedy(requestsPerMinute, Duration.ofMinutes(1))
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private boolean isPublicPath(String path) {
        for (String pattern : PUBLIC_PATHS) {
            if (PATH_MATCHER.match(pattern, path)) return true;
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(message));
    }
}
