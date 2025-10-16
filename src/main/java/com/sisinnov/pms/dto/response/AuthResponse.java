package com.sisinnov.pms.dto.response;

import com.sisinnov.pms.enums.UserRole;

public record AuthResponse(
        String token,
        String type,
        String username,
        UserRole role
) {
    public AuthResponse(String token, String username, UserRole role) {
        this(token, "Bearer", username, role);
    }
}