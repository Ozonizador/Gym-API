package com.gym.gym_api.controller;

import com.gym.gym_api.TestcontainersConfiguration;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void login_shouldReturnJwtForValidCredentials() throws Exception {

        User user = new User();
        user.setUsername("login-user");
        user.setEmail("login-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        userRepository.save(user);

        String requestBody = """
                {
                    "username": "login-user",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.token").isNotEmpty()
                );
    }

    @Test
    void login_shouldReturnUnauthorizedForInvalidCredentials() throws Exception {

        User user = new User();
        user.setUsername("invalid-login-user");
        user.setEmail("invalid-login-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        userRepository.save(user);

        String requestBody = """
                {
                    "username": "invalid-login-user",
                    "password": "wrong-password"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_shouldReturn400WhenUsernameIsBlank() throws Exception {

        String requestBody = """
                {
                    "username": "",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400WhenPasswordIsBlank() throws Exception {

        String requestBody = """
                {
                    "username": "login-user",
                    "password": ""
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}