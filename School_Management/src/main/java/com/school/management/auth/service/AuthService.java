package com.school.management.auth.service;

import com.school.management.auth.config.JwtProperties;
import com.school.management.auth.dto.ChangePasswordRequest;
import com.school.management.auth.dto.LoginRequest;
import com.school.management.auth.dto.LoginResponse;
import com.school.management.auth.entity.PasswordResetOtp;
import com.school.management.auth.repository.PasswordResetOtpRepository;
import com.school.management.common.exception.BadRequestException;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.common.service.EmailService;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
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

import java.security.SecureRandom;
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
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final EmailService emailService;
    private final PasswordResetOtpRepository otpRepository;

    // ── Login (Multi-Identifier) ───────────────────────────────────────────────

    /**
     * Resolves identifier (email / username / rollNumber) to an email,
     * then authenticates with Spring Security.
     */
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        String resolvedEmail = resolveIdentifierToEmail(loginRequest.getIdentifier());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(resolvedEmail, loginRequest.getPassword()));
        } catch (DisabledException e) {
            throw new BadRequestException("Your account is suspended. Please contact the admin.");
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid credentials. Check your identifier or password.");
        }

        User user = userRepository.findByEmail(resolvedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", resolvedEmail));

        UserDetails userDetails = userDetailsService.loadUserByUsername(resolvedEmail);

        String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        redisTokenService.storeRefreshToken(user.getEmail(), refreshToken, jwtProperties.getRefreshExpirationMs());

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

    /**
     * Resolves email / username / rollNumber → email.
     * Priority: exact email match → username match → student rollNumber match.
     */
    private String resolveIdentifierToEmail(String identifier) {
        // 1. Try email
        var byEmail = userRepository.findByEmail(identifier);
        if (byEmail.isPresent())
            return byEmail.get().getEmail();

        // 2. Try username
        var byUsername = userRepository.findByUsername(identifier);
        if (byUsername.isPresent())
            return byUsername.get().getEmail();

        // 3. Try student rollNumber (e.g. "STU-2024-001")
        var byRoll = studentRepository.findByRollNumber(identifier);
        if (byRoll.isPresent())
            return byRoll.get().getUser().getEmail();

        throw new BadRequestException("No account found with identifier: " + identifier);
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
        user.setIsFirstLogin(false);
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // ── Password Reset (OTP Flow) ─────────────────────────────────────────────

    /**
     * Step 1: Generates a 6-digit OTP and emails it to the user.
     * OTP expires in 15 minutes.
     */
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException(
                        "No account found with that email. Check your email or contact admin."));

        // Invalidate any previous unused OTPs
        otpRepository.invalidateAllForEmail(email);

        // Generate a secure 6-digit OTP
        String otp = String.format("%06d", new SecureRandom().nextInt(999999));

        otpRepository.save(PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build());

        emailService.sendPasswordResetOtpEmail(email, user.getFullName(), otp);
        log.info("Password reset OTP sent to: {}", email);
    }

    /**
     * Step 2: Verifies the OTP and resets the password.
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        PasswordResetOtp otpEntity = otpRepository
                .findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException(
                        "No active OTP found for this email. Please request a new one."));

        if (LocalDateTime.now().isAfter(otpEntity.getExpiresAt())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (!otpEntity.getOtp().equals(otp)) {
            throw new BadRequestException("Invalid OTP. Please check and try again.");
        }

        // Mark OTP as used
        otpEntity.setIsUsed(true);
        otpRepository.save(otpEntity);

        // Update password
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setIsFirstLogin(false);
        userRepository.save(user);

        log.info("Password reset successfully for: {}", email);
    }

    // ── Profile ───────────────────────────────────────────────────────────────

    /**
     * Returns the full User entity for the /auth/me endpoint.
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
