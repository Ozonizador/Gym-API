package com.gym.gym_api.service;

import com.gym.gym_api.dto.auth.LoginRequest;
import com.gym.gym_api.dto.auth.LoginResponse;
import com.gym.gym_api.exception.InvalidCredentialsException;
import com.gym.gym_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                authenticationManager,
                jwtService
        );
    }

    @Test
    void login_shouldReturnJwtTokenWhenCredentialsAreValid() {

        LoginRequest request = mock(LoginRequest.class);

        when(request.getUsername())
                .thenReturn("john");

        when(request.getPassword())
                .thenReturn("password123");

        when(jwtService.generateToken("john"))
                .thenReturn("jwt-token");

        LoginResponse response =
                authService.login(request);

        assertEquals(
                "jwt-token",
                response.getToken()
        );

        verify(authenticationManager)
                .authenticate(
                        any(UsernamePasswordAuthenticationToken.class)
                );

        verify(jwtService)
                .generateToken("john");
    }

    @Test
    void login_shouldThrowInvalidCredentialsExceptionWhenCredentialsAreInvalid() {

        LoginRequest request = mock(LoginRequest.class);

        when(request.getUsername())
                .thenReturn("john");

        when(request.getPassword())
                .thenReturn("wrong-password");

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );
    }
}