package com.school.management.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Login DTO — accepts email, username, or student rollNumber as identifier.
 * Password is always required.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Identifier (email / username / roll number) is required")
    private String identifier; // email, username, or rollNumber

    @NotBlank(message = "Password is required")
    private String password;
}
