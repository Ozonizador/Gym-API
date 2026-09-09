package com.gym.gym_api.service;

import com.gym.gym_api.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-must-be-at-least-32-bytes-long",
                3600000
        );
    }

    @Test
    void generateToken_shouldContainCorrectUsername() {

        String token = jwtService.generateToken("john");

        String username =
                jwtService.extractUsername(token);

        assertEquals("john", username);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {

        String token = jwtService.generateToken("john");

        boolean valid =
                jwtService.isTokenValid(token, "john");

        assertTrue(valid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForDifferentUsername() {

        String token = jwtService.generateToken("john");

        boolean valid =
                jwtService.isTokenValid(token, "jane");

        assertFalse(valid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForTamperedToken() {

        String token = jwtService.generateToken("john");

        String tamperedToken =
                token.substring(0, token.length() - 2) + "xx";

        assertFalse(
                jwtService.isTokenValid(
                        tamperedToken,
                        "john"
                )
        );
    }
}