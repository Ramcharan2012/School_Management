package com.school.management.auth.controller;

import com.school.management.auth.dto.ChangePasswordRequest;
import com.school.management.auth.dto.LoginRequest;
import com.school.management.auth.dto.LoginResponse;
import com.school.management.auth.service.AuthService;
import com.school.management.auth.service.JwtService;
import com.school.management.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login, logout, token refresh, and password change")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    // ── Public — No Auth Required ─────────────────────────────────────────────

    @PostMapping("/login")
    @Operation(summary = "Login with email / username / roll number + password")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        String message = Boolean.TRUE.equals(response.getIsFirstLogin())
                ? "Login successful. Please change your password — this is your first login."
                : "Login successful.";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Get new access token using a valid refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity
                .ok(ApiResponse.success("Token refreshed.", authService.refreshToken(request.getRefreshToken())));
    }

    // ── Protected — Requires Valid JWT ────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Logout — blacklists current access token in Redis", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(authHeader.substring(7), userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully."));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password (mandatory on first login)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserId(authHeader.substring(7));
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully. Please login again."));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user info", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserDetails>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userDetails));
    }

    // ── Password Reset (Public — no token needed) ─────────────────────────────

    @PostMapping("/password-reset/request")
    @Operation(summary = "Request password reset — sends a 6-digit OTP to the user's email")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(
                "OTP sent to your email. It expires in 15 minutes."));
    }

    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Reset password using OTP received via email")
    public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
            @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully. Please login with your new password."));
    }

    // ── Inner DTOs ────────────────────────────────────────────────────────────

    @lombok.Data
    static class RefreshTokenRequest {
        private String refreshToken;
    }

    @lombok.Data
    static class PasswordResetRequest {
        private String email;
    }

    @lombok.Data
    static class PasswordResetConfirmRequest {
        private String email;
        private String otp;
        private String newPassword;
    }
}
