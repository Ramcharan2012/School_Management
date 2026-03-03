package com.school.management.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-backed token blacklist service.
 *
 * When a user logs out, their JWT is stored in Redis with a TTL equal to
 * the token's remaining validity period. This prevents re-use of tokens
 * after logout — solving the stateless JWT logout problem.
 *
 * Key pattern: "blacklist:{token}"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String REFRESH_PREFIX = "refresh:";

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Blacklists an access token with TTL equal to its remaining validity.
     * After TTL expires, Redis auto-removes the entry (no cleanup needed).
     */
    public void blacklistToken(String token, long remainingValidityMs) {
        if (remainingValidityMs <= 0)
            return;
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "REVOKED", remainingValidityMs, TimeUnit.MILLISECONDS);
        log.info("Token blacklisted. TTL: {}ms", remainingValidityMs);
    }

    /**
     * Checks if a token has been blacklisted (i.e., the user has logged out).
     */
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    /**
     * Stores a refresh token mapped to user email for refresh flow.
     */
    public void storeRefreshToken(String email, String refreshToken, long expiryMs) {
        redisTemplate.opsForValue().set(REFRESH_PREFIX + email, refreshToken, expiryMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Validates that the refresh token matches what's stored for this email.
     */
    public boolean isRefreshTokenValid(String email, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(REFRESH_PREFIX + email);
        return refreshToken.equals(stored);
    }

    /**
     * Removes refresh token on logout.
     */
    public void deleteRefreshToken(String email) {
        redisTemplate.delete(REFRESH_PREFIX + email);
    }
}
