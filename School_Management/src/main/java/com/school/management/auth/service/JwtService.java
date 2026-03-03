package com.school.management.auth.service;

import com.school.management.auth.config.JwtProperties;
import com.school.management.common.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Central JWT utility service.
 * Handles token generation, validation, and claim extraction.
 * Uses HMAC-SHA256 (HS256) signing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        // Ensure the secret key is at least 256 bits (32 chars) for HS256
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Token Generation ──────────────────────────────────────────────────────

    public String generateAccessToken(UserDetails userDetails, Long userId, Role role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        extraClaims.put("role", role.name());
        extraClaims.put("tokenType", "ACCESS");
        return buildToken(extraClaims, userDetails.getUsername(), jwtProperties.getExpirationMs());
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("tokenType", "REFRESH");
        return buildToken(extraClaims, userDetails.getUsername(), jwtProperties.getRefreshExpirationMs());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    // ── Token Validation ──────────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT token malformed: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims empty: {}", e.getMessage());
        }
        return false;
    }

    // ── Claim Extraction ──────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getRemainingValidityMs(String token) {
        Date expiration = extractExpiration(token);
        return Math.max(0, expiration.getTime() - System.currentTimeMillis());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = parseClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
