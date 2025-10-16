package com.sisinnov.pms.service.impl;

import com.sisinnov.pms.dto.request.LoginRequest;
import com.sisinnov.pms.dto.request.RegisterRequest;
import com.sisinnov.pms.dto.response.AuthResponse;
import com.sisinnov.pms.entity.User;
import com.sisinnov.pms.enums.UserRole;
import com.sisinnov.pms.exception.AuthenticationException;
import com.sisinnov.pms.exception.BusinessException;
import com.sisinnov.pms.mapper.UserMapper;
import com.sisinnov.pms.repository.UserRepository;
import com.sisinnov.pms.security.JwtTokenProvider;
import com.sisinnov.pms.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

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

        return new AuthResponse(token, user.getUsername(), user.getRole());
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

            return new AuthResponse(token, user.getUsername(), user.getRole());
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Invalid username or password");
        }
    }
}