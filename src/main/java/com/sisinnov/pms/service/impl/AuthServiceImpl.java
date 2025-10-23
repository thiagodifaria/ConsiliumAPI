package com.sisinnov.pms.service.impl;

import com.sisinnov.pms.dto.request.LoginRequest;
import com.sisinnov.pms.dto.request.RefreshTokenRequest;
import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.dto.response.AuthResponse;
import com.sisinnov.pms.entity.User;
import com.sisinnov.pms.enums.UserRole;
import com.sisinnov.pms.exception.AuthenticationException;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.mapper.UserMapper;
import com.sisinnov.pms.repository.RefreshTokenRepository;
import com.sisinnov.pms.repository.UserRepository;
import com.sisinnov.pms.security.JwtTokenProvider;
import com.sisinnov.pms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already exists: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already exists: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.USER);
        user.setActive(true);

        user = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.password()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = generateAndSaveRefreshToken(user.getId());

        return new AuthResponse(token, refreshToken, user.getUsername(), user.getRole());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.username(),
                            request.password()
                    )
            );

            String token = jwtTokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(request.username())
                    .orElseThrow(() -> new AuthenticationException("User not found"));

            String refreshToken = generateAndSaveRefreshToken(user.getId());

            return new AuthResponse(token, refreshToken, user.getUsername(), user.getRole());
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        UUID userId = refreshTokenRepository.findUserIdByToken(request.refreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired refresh token"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!user.getActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        String newRefreshToken = generateAndSaveRefreshToken(user.getId());

        refreshTokenRepository.deleteByToken(request.refreshToken());

        return new AuthResponse(newAccessToken, newRefreshToken, user.getUsername(), user.getRole());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.deleteByToken(refreshToken);
    }

    @Override
    public void logoutAllDevices(UUID userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }

    private String generateAndSaveRefreshToken(UUID userId) {
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenRepository.save(refreshToken, userId);
        return refreshToken;
    }
}