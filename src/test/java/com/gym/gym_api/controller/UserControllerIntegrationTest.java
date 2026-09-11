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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void createUser_shouldCreateUserSuccessfully() throws Exception {

        String requestBody = """
                {
                    "username": "new-user",
                    "email": "new-user@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username")
                        .value("new-user"))
                .andExpect(jsonPath("$.email")
                        .value("new-user@example.com"))
                .andExpect(jsonPath("$.createdAt")
                        .isNotEmpty());

        User savedUser =
                userRepository.findByUsername("new-user")
                        .orElseThrow();

        assertEquals(
                "new-user",
                savedUser.getUsername()
        );

        assertEquals(
                "new-user@example.com",
                savedUser.getEmail()
        );

        assertNotEquals(
                "password123",
                savedUser.getPasswordHash()
        );

        assertTrue(
                savedUser.getPasswordHash() != null
                        && !savedUser.getPasswordHash().isBlank()
        );
    }

    @Test
    void createUser_shouldReturn400WhenUsernameIsBlank() throws Exception {

        String requestBody = """
                {
                    "username": "",
                    "email": "valid@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400WhenEmailIsInvalid() throws Exception {

        String requestBody = """
                {
                    "username": "valid-user",
                    "email": "not-an-email",
                    "password": "password123"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_shouldReturn400WhenPasswordIsTooShort() throws Exception {

        String requestBody = """
                {
                    "username": "valid-user",
                    "email": "valid-password@example.com",
                    "password": "short"
                }
                """;

        mockMvc.perform(
                        post("/api/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsers_shouldReturn401WithoutAuthentication() throws Exception {

        mockMvc.perform(
                        get("/api/users")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_shouldReturnUsersForAuthenticatedUser() throws Exception {

        User firstUser = new User();
        firstUser.setUsername("users-test-one");
        firstUser.setEmail("users-test-one@example.com");
        firstUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User secondUser = new User();
        secondUser.setUsername("users-test-two");
        secondUser.setEmail("users-test-two@example.com");
        secondUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedFirstUser = userRepository.save(firstUser);
        userRepository.save(secondUser);

        String token = jwtService.generateToken(
                savedFirstUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/users")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].username")
                        .isNotEmpty());
    }
}