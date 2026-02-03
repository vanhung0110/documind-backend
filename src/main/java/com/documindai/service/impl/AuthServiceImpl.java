package com.documindai.service.impl;

import com.documindai.dto.request.LoginRequest;
import com.documindai.dto.request.RegisterRequest;
import com.documindai.dto.response.AuthResponse;
import com.documindai.exception.BadRequestException;
import com.documindai.exception.UnauthorizedException;
import com.documindai.model.Role;
import com.documindai.model.User;
import com.documindai.repository.UserRepository;
import com.documindai.security.JwtTokenProvider;
import com.documindai.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation của AuthService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.admin.default.username}")
    private String defaultAdminUsername;

    @Value("${app.admin.default.password}")
    private String defaultAdminPassword;

    @Value("${app.admin.default.email}")
    private String defaultAdminEmail;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.getActive()) {
            throw new UnauthorizedException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        log.info("User {} logged in successfully", user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().toString());

        return response;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Validate
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(Role.ROLE_USER); // Mặc định là USER
        user.setActive(true);

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        log.info("User {} registered successfully", user.getUsername());

        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().toString());

        return response;
    }

    @Override
    @Transactional
    public void createDefaultAdmin() {
        if (!userRepository.existsByUsername(defaultAdminUsername)) {
            log.info("Creating default admin account");

            User admin = new User();
            admin.setUsername(defaultAdminUsername);
            admin.setEmail(defaultAdminEmail);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setFullName("System Administrator");
            admin.setRole(Role.ROLE_ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            log.info("Default admin account created successfully");
        } else {
            log.info("Default admin account already exists");
        }
    }
}
