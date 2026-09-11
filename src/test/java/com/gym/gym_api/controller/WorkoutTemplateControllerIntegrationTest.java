package com.gym.gym_api.controller;

import com.gym.gym_api.TestcontainersConfiguration;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@Transactional
class WorkoutTemplateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getAllTemplates_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/workout-templates")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllTemplates_shouldReturnTemplatesForAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("template-list-user");
        user.setEmail("template-list-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedUser);
        template.setName("Push Template");
        template.setDescription("Chest and shoulders");
        template.setCreatedAt(LocalDateTime.now());

        workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workout-templates")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void createTemplate_shouldCreateTemplateSuccessfully() throws Exception {

        User user = new User();
        user.setUsername("template-create-user");
        user.setEmail("template-create-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        Exercise exercise = new Exercise();
        exercise.setName("Bench Press Integration Test");
        exercise.setDescription("Integration test exercise");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Push Template",
                    "description": "Chest and shoulders",
                    "exercises": [
                        {
                            "exerciseId": %d,
                            "position": 1,
                            "targetSets": 4,
                            "targetReps": 10
                        }
                    ]
                }
                """.formatted(savedExercise.getId());

        mockMvc.perform(
                        post("/api/workout-templates")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.name")
                                .value("Push Template")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Chest and shoulders")
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(savedUser.getId())
                )
                .andExpect(
                        jsonPath("$.exercises")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.exercises[0].exerciseId")
                                .value(savedExercise.getId())
                )
                .andExpect(
                        jsonPath("$.exercises[0].exerciseName")
                                .value("Bench Press Integration Test")
                )
                .andExpect(
                        jsonPath("$.exercises[0].position")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.exercises[0].targetSets")
                                .value(4)
                )
                .andExpect(
                        jsonPath("$.exercises[0].targetReps")
                                .value(10)
                );

        List<WorkoutTemplate> templates =
                workoutTemplateRepository.findAll();

        WorkoutTemplate savedTemplate = templates.stream()
                .filter(template ->
                        "Push Template".equals(template.getName())
                                && savedUser.getId().equals(
                                template.getUser().getId()
                        )
                )
                .findFirst()
                .orElseThrow();

        assertEquals(
                "Push Template",
                savedTemplate.getName()
        );

        assertEquals(
                savedUser.getId(),
                savedTemplate.getUser().getId()
        );
    }

    @Test
    void getTemplateById_shouldReturnTemplateOwnedByAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("template-get-user");
        user.setEmail("template-get-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedUser);
        template.setName("My Template");
        template.setDescription("My description");
        template.setCreatedAt(LocalDateTime.now());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workout-templates/" + savedTemplate.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("My Template")
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(savedUser.getId())
                );
    }

    @Test
    void getTemplateById_shouldReturn404WhenTemplateBelongsToAnotherUser() throws Exception {

        User owner = new User();
        owner.setUsername("template-owner-get");
        owner.setEmail("template-owner-get@example.com");
        owner.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOwner = userRepository.save(owner);

        User otherUser = new User();
        otherUser.setUsername("template-other-get");
        otherUser.setEmail("template-other-get@example.com");
        otherUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOtherUser = userRepository.save(otherUser);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedOwner);
        template.setName("Private Template");
        template.setDescription("Private");
        template.setCreatedAt(LocalDateTime.now());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedOtherUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workout-templates/" + savedTemplate.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTemplate_shouldUpdateTemplateOwnedByAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("template-update-user");
        user.setEmail("template-update-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        Exercise exercise = new Exercise();
        exercise.setName("Shoulder Press Update Test");
        exercise.setDescription("Update integration test exercise");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedUser);
        template.setName("Old Template");
        template.setDescription("Old description");
        template.setCreatedAt(LocalDateTime.now());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Updated Template",
                    "description": "Updated description",
                    "exercises": [
                        {
                            "exerciseId": %d,
                            "position": 1,
                            "targetSets": 4,
                            "targetReps": 10
                        }
                    ]
                }
                """.formatted(savedExercise.getId());

        mockMvc.perform(
                        put("/api/workout-templates/" + savedTemplate.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.name")
                                .value("Updated Template")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Updated description")
                )
                .andExpect(
                        jsonPath("$.exercises[0].exerciseId")
                                .value(savedExercise.getId())
                )
                .andExpect(
                        jsonPath("$.exercises[0].position")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.exercises[0].targetSets")
                                .value(4)
                )
                .andExpect(
                        jsonPath("$.exercises[0].targetReps")
                                .value(10)
                );

        WorkoutTemplate updatedTemplate =
                workoutTemplateRepository.findById(
                        savedTemplate.getId()
                ).orElseThrow();

        assertEquals(
                "Updated Template",
                updatedTemplate.getName()
        );

        assertEquals(
                "Updated description",
                updatedTemplate.getDescription()
        );

        assertEquals(
                savedUser.getId(),
                updatedTemplate.getUser().getId()
        );
    }

    @Test
    void deleteTemplate_shouldDeleteTemplateOwnedByAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("template-delete-user");
        user.setEmail("template-delete-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedUser);
        template.setName("Template To Delete");
        template.setDescription("Delete me");
        template.setCreatedAt(LocalDateTime.now());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        delete(
                                "/api/workout-templates/"
                                        + savedTemplate.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        assertTrue(
                workoutTemplateRepository.findById(
                        savedTemplate.getId()
                ).isEmpty()
        );
    }

    @Test
    void updateTemplate_shouldReturn404WhenTemplateBelongsToAnotherUser() throws Exception {

        User owner = new User();
        owner.setUsername("template-update-owner");
        owner.setEmail("template-update-owner@example.com");
        owner.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOwner = userRepository.save(owner);

        User otherUser = new User();
        otherUser.setUsername("template-update-other");
        otherUser.setEmail("template-update-other@example.com");
        otherUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOtherUser = userRepository.save(otherUser);

        Exercise exercise = new Exercise();
        exercise.setName("Template Update Ownership Test");
        exercise.setDescription("Ownership test exercise");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedOwner);
        template.setName("Private Template");
        template.setDescription("Private");
        template.setCreatedAt(LocalDateTime.now());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedOtherUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Unauthorized Update",
                    "description": "Should not be allowed",
                    "exercises": [
                        {
                            "exerciseId": %d,
                            "position": 1,
                            "targetSets": 3,
                            "targetReps": 8
                        }
                    ]
                }
                """.formatted(savedExercise.getId());

        mockMvc.perform(
                        put("/api/workout-templates/" + savedTemplate.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTemplate_shouldReturn404WhenTemplateBelongsToAnotherUser() throws Exception {

        User owner = new User();
        owner.setUsername("template-delete-owner");
        owner.setEmail("template-delete-owner@example.com");
        owner.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOwner = userRepository.save(owner);

        User otherUser = new User();
        otherUser.setUsername("template-delete-other");
        otherUser.setEmail("template-delete-other@example.com");
        otherUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOtherUser = userRepository.save(otherUser);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedOwner);
        template.setName("Protected Template");
        template.setDescription("Should not be deleted");
        template.setCreatedAt(LocalDateTime.now());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedOtherUser.getUsername()
        );

        mockMvc.perform(
                        delete("/api/workout-templates/" + savedTemplate.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        assertTrue(
                workoutTemplateRepository.findById(
                        savedTemplate.getId()
                ).isPresent()
        );
    }
}