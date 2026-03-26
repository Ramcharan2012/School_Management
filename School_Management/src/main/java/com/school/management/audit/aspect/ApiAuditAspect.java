package com.school.management.audit.aspect;

import com.school.management.audit.entity.ApiAuditLog;
import com.school.management.audit.repository.ApiAuditLogRepository;
import com.school.management.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

/**
 * AOP Aspect that intercepts ALL REST controller methods and saves an audit log
 * entry.
 *
 * Uses @Around so it captures both the request and response status as well as
 * duration.
 * Saves asynchronously via a helper to avoid slowing down the API response.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiAuditAspect {

    private final ApiAuditLogRepository auditLogRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    /**
     * Intercepts every method in any @RestController under com.school.management.
     */
    @Around("within(@org.springframework.web.bind.annotation.RestController *) " +
            "&& execution(* com.school.management..*Controller.*(..))")
    public Object auditApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime timestamp = LocalDateTime.now();

        // Extract request info
        HttpServletRequest request = null;
        String httpMethod = "UNKNOWN";
        String endpoint = "UNKNOWN";
        String ipAddress = "UNKNOWN";
        String requestBody = null;

        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                request = attrs.getRequest();
                httpMethod = request.getMethod();
                endpoint = request.getRequestURI();
                ipAddress = getClientIp(request);
            }
        } catch (Exception ignored) {
            /* Non-critical — don't fail the request */ }

        // Extract user from SecurityContext
        Long userId = null;
        String username = null;
        String role = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String authHeader = request != null ? request.getHeader("Authorization") : null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    userId = jwtService.extractUserId(token);
                    role = jwtService.extractRole(token);
                }
                username = auth.getName();
            }
        } catch (Exception ignored) {
            /* Don't fail if token extraction fails */ }

        // Try to serialize the first @RequestBody argument
        try {
            for (Object arg : joinPoint.getArgs()) {
                if (arg != null && !(arg instanceof org.springframework.security.core.userdetails.UserDetails)) {
                    requestBody = objectMapper.writeValueAsString(arg);
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        // Execute the actual controller method
        int responseStatus = 200;
        String errorMessage = null;
        Object result = null;

        try {
            result = joinPoint.proceed();
            // Try to get status from ResponseEntity
            if (result instanceof org.springframework.http.ResponseEntity<?> re) {
                responseStatus = re.getStatusCode().value();
            }
        } catch (Throwable ex) {
            responseStatus = 500;
            errorMessage = ex.getMessage();
            throw ex; // Re-throw so GlobalExceptionHandler handles it
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            saveAuditLog(timestamp, userId, username, role, httpMethod,
                    endpoint, requestBody, responseStatus, durationMs, ipAddress, errorMessage);
        }

        return result;
    }

    @Async
    protected void saveAuditLog(LocalDateTime timestamp, Long userId, String username,
            String role, String httpMethod, String endpoint,
            String requestBody, int responseStatus, long durationMs,
            String ipAddress, String errorMessage) {
        try {
            auditLogRepository.save(ApiAuditLog.builder()
                    .timestamp(timestamp)
                    .userId(userId)
                    .username(username)
                    .role(role)
                    .httpMethod(httpMethod)
                    .endpoint(endpoint)
                    .requestBody(requestBody)
                    .responseStatus(responseStatus)
                    .durationMs(durationMs)
                    .ipAddress(ipAddress)
                    .errorMessage(errorMessage)
                    .build());
        } catch (Exception e) {
            log.warn("Failed to save API audit log: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
