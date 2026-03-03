package com.school.management.auth.dto;

import com.school.management.common.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresInMs;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private Role role;
    private Boolean isFirstLogin;
}
