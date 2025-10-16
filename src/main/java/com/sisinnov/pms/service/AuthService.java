package com.sisinnov.pms.service;

import com.sisinnov.pms.dto.request.LoginRequest;
import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}