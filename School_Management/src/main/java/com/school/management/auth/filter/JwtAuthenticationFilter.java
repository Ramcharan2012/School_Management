package com.school.management.auth.filter;

import com.school.management.auth.service.CustomUserDetailsService;
import com.school.management.auth.service.JwtService;
import com.school.management.auth.service.RedisTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter — runs once per request.
 *
 * Flow:
 * 1. Extract "Bearer {token}" from Authorization header
 * 2. Validate token signature + expiry
 * 3. Check Redis blacklist (covers logged-out users)
 * 4. Load UserDetails from DB
 * 5. Set authenticated principal in SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RedisTokenService redisTokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        try {
            // 1. Validate JWT signature and expiry
            if (!jwtService.validateToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Check Redis blacklist — catches logged-out tokens
            if (redisTokenService.isTokenBlacklisted(jwt)) {
                log.warn("Attempt to use blacklisted token from IP: {}", request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            // 3. Extract email and load user
            final String email = jwtService.extractEmail(jwt);
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            log.error("JWT filter error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
