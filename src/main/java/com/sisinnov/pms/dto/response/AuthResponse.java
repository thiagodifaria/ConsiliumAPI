package com.sisinnov.pms.dto.response;

import com.sisinnov.pms.enums.UserRole;

public record AuthResponse(
        String token,
        String refreshToken,
        String type,
        String username,
        UserRole role
) {
    public AuthResponse(String token, String refreshToken, String username, UserRole role) {
        this(token, refreshToken, "Bearer", username, role);
    }
}