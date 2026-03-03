package com.school.management.auth.service;

import com.school.management.auth.config.JwtProperties;
import com.school.management.auth.dto.ChangePasswordRequest;
import com.school.management.auth.dto.LoginRequest;
import com.school.management.auth.dto.LoginResponse;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.user.entity.User;
import com.school.management.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisTokenService redisTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    // ── Login ─────────────────────────────────────────────────────────────────

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(), loginRequest.getPassword()));
        } catch (DisabledException e) {
            throw new BadRequestException("Your account is suspended. Please contact the admin.");
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getEmail()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Store refresh token in Redis
        redisTokenService.storeRefreshToken(user.getEmail(), refreshToken, jwtProperties.getRefreshExpirationMs());

        // Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("User logged in: {} [{}]", user.getEmail(), user.getRole());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtProperties.getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    public void logout(String accessToken, String email) {
        long remainingMs = jwtService.getRemainingValidityMs(accessToken);
        redisTokenService.blacklistToken(accessToken, remainingMs);
        redisTokenService.deleteRefreshToken(email);
        log.info("User logged out: {}", email);
    }

    // ── Refresh Token ─────────────────────────────────────────────────────────

    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token.");
        }

        String email = jwtService.extractEmail(refreshToken);
        if (!redisTokenService.isRefreshTokenValid(email, refreshToken)) {
            throw new BadRequestException("Refresh token has been revoked. Please login again.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String newAccessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole());
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Rotate refresh token in Redis
        redisTokenService.storeRefreshToken(email, newRefreshToken, jwtProperties.getRefreshExpirationMs());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtProperties.getExpirationMs())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .isFirstLogin(user.getIsFirstLogin())
                .build();
    }

    // ── Change Password ───────────────────────────────────────────────────────

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setIsFirstLogin(false); // clear first-login flag
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }
}
