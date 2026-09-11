package com.gym.gym_api.service;

import com.gym.gym_api.dto.auth.LoginRequest;
import com.gym.gym_api.dto.auth.LoginResponse;
import com.gym.gym_api.exception.InvalidCredentialsException;
import com.gym.gym_api.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private static final Logger log =
            LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

        } catch (BadCredentialsException exception) {

            log.warn("Failed authentication attempt for user '{}'",
                    request.getUsername());
            
            throw new InvalidCredentialsException(
                    "Invalid username or password"
            );
        }

        log.info("User '{}' authenticated successfully", request.getUsername());

        String token = jwtService.generateToken(
                request.getUsername()
        );

        return new LoginResponse(token);
    }
}