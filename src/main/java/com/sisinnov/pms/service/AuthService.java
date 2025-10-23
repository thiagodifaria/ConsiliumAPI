package com.sisinnov.pms.service;

import com.sisinnov.pms.dto.request.LoginRequest;
import com.sisinnov.pms.dto.request.RefreshTokenRequest;
import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.dto.response.AuthResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(String refreshToken);

    void logoutAllDevices(UUID userId);
}