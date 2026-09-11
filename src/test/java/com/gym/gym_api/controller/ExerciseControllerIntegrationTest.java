package com.gym.gym_api.controller;

import com.gym.gym_api.TestcontainersConfiguration;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.MuscleGroup;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.MuscleGroupRepository;
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

import java.time.LocalDateTime;

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
class ExerciseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MuscleGroupRepository muscleGroupRepository;

    @Test
    void getAllExercises_shouldReturnExercises()
            throws Exception {

        Exercise exercise = new Exercise();
        exercise.setName("Integration Bench Press");
        exercise.setDescription("Integration test exercise");
        exercise.setCreatedAt(LocalDateTime.now());

        exerciseRepository.save(exercise);

        String token = createToken("exercise-list-user");

        mockMvc.perform(
                        get("/api/exercises")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].name")
                        .isNotEmpty());
    }

    @Test
    void getExerciseById_shouldReturnExercise()
            throws Exception {

        Exercise exercise = new Exercise();
        exercise.setName("Integration Squat");
        exercise.setDescription("Integration test squat");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        String token = createToken("exercise-get-user");

        mockMvc.perform(
                        get("/api/exercises/" + savedExercise.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(savedExercise.getId())
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Integration Squat")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Integration test squat")
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .isNotEmpty()
                );
    }

    @Test
    void getExerciseById_shouldReturn404WhenExerciseDoesNotExist()
            throws Exception {

        String token = createToken("exercise-missing-user");

        mockMvc.perform(
                        get("/api/exercises/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createExercise_shouldCreateExerciseSuccessfully()
            throws Exception {

        String token = createToken("exercise-create-user");

        String requestBody = """
                {
                    "name": "Integration Deadlift",
                    "description": "Integration test deadlift"
                }
                """;

        mockMvc.perform(
                        post("/api/exercises")
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
                                .value("Integration Deadlift")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Integration test deadlift")
                )
                .andExpect(
                        jsonPath("$.id")
                                .isNumber()
                )
                .andExpect(
                        jsonPath("$.createdAt")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.muscleGroups")
                                .isArray()
                );

        Exercise savedExercise = exerciseRepository.findAll()
                .stream()
                .filter(exercise ->
                        "Integration Deadlift".equals(
                                exercise.getName()
                        )
                )
                .findFirst()
                .orElseThrow();

        assertEquals(
                "Integration Deadlift",
                savedExercise.getName()
        );

        assertEquals(
                "Integration test deadlift",
                savedExercise.getDescription()
        );

        assertTrue(
                savedExercise.getCreatedAt() != null
        );

        assertTrue(
                savedExercise.getMuscleGroups().isEmpty()
        );
    }

    @Test
    void createExercise_shouldReturn409WhenNameAlreadyExists()
            throws Exception {

        Exercise exercise = new Exercise();
        exercise.setName("Duplicate Integration Exercise");
        exercise.setDescription("Existing exercise");
        exercise.setCreatedAt(LocalDateTime.now());

        exerciseRepository.save(exercise);

        String token = createToken("exercise-duplicate-user");

        String requestBody = """
                {
                    "name": "Duplicate Integration Exercise",
                    "description": "Duplicate attempt"
                }
                """;

        mockMvc.perform(
                        post("/api/exercises")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void createExercise_shouldReturn400WhenNameIsBlank()
            throws Exception {

        String token = createToken("exercise-validation-user");

        String requestBody = """
                {
                    "name": "",
                    "description": "Invalid exercise"
                }
                """;

        mockMvc.perform(
                        post("/api/exercises")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExercise_shouldUpdateExerciseSuccessfully()
            throws Exception {

        Exercise exercise = new Exercise();
        exercise.setName("Old Integration Exercise");
        exercise.setDescription("Old description");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        String token = createToken("exercise-update-user");

        String requestBody = """
                {
                    "name": "Updated Integration Exercise",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(
                        put("/api/exercises/" + savedExercise.getId())
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
                                .value("Updated Integration Exercise")
                )
                .andExpect(
                        jsonPath("$.description")
                                .value("Updated description")
                );

        Exercise updatedExercise =
                exerciseRepository.findById(
                        savedExercise.getId()
                ).orElseThrow();

        assertEquals(
                "Updated Integration Exercise",
                updatedExercise.getName()
        );

        assertEquals(
                "Updated description",
                updatedExercise.getDescription()
        );
    }

    @Test
    void updateExercise_shouldReturn404WhenExerciseDoesNotExist()
            throws Exception {

        String token = createToken("exercise-update-missing-user");

        String requestBody = """
                {
                    "name": "Updated Exercise",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(
                        put("/api/exercises/999999")
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
    void updateExercise_shouldReturn409WhenNewNameAlreadyExists()
            throws Exception {

        Exercise firstExercise = new Exercise();
        firstExercise.setName("First Integration Exercise");
        firstExercise.setDescription("First");
        firstExercise.setCreatedAt(LocalDateTime.now());

        Exercise secondExercise = new Exercise();
        secondExercise.setName("Second Integration Exercise");
        secondExercise.setDescription("Second");
        secondExercise.setCreatedAt(LocalDateTime.now());

        Exercise savedFirstExercise =
                exerciseRepository.save(firstExercise);

        exerciseRepository.save(secondExercise);

        String token = createToken("exercise-update-duplicate-user");

        String requestBody = """
                {
                    "name": "Second Integration Exercise",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(
                        put(
                                "/api/exercises/"
                                        + savedFirstExercise.getId()
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void deleteExercise_shouldDeleteExerciseSuccessfully()
            throws Exception {

        Exercise exercise = new Exercise();
        exercise.setName("Delete Integration Exercise");
        exercise.setDescription("Delete me");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        String token = createToken("exercise-delete-user");

        mockMvc.perform(
                        delete("/api/exercises/" + savedExercise.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        assertTrue(
                exerciseRepository.findById(
                        savedExercise.getId()
                ).isEmpty()
        );
    }

    @Test
    void deleteExercise_shouldReturn404WhenExerciseDoesNotExist()
            throws Exception {

        String token = createToken("exercise-delete-missing-user");

        mockMvc.perform(
                        delete("/api/exercises/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    private String createToken(String username) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        return jwtService.generateToken(
                savedUser.getUsername()
        );
    }

    @Test
    void createExercise_shouldCreateExerciseWithMuscleGroups()
            throws Exception {

        MuscleGroup chest = new MuscleGroup();
        chest.setName("Integration Chest");
        chest = muscleGroupRepository.save(chest);

        MuscleGroup triceps = new MuscleGroup();
        triceps.setName("Integration Triceps");
        triceps = muscleGroupRepository.save(triceps);

        String token = createToken("exercise-muscle-create-user");

        String requestBody = """
                {
                    "name": "Integration Bench With Muscles",
                    "description": "Bench press with muscle groups",
                    "muscleGroups": [
                        {
                            "muscleGroupId": %d,
                            "role": "PRIMARY"
                        },
                        {
                            "muscleGroupId": %d,
                            "role": "SECONDARY"
                        }
                    ]
                }
                """.formatted(
                chest.getId(),
                triceps.getId()
        );

        mockMvc.perform(
                        post("/api/exercises")
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
                                .value("Integration Bench With Muscles")
                )
                .andExpect(
                        jsonPath("$.muscleGroups")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.muscleGroups.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.muscleGroups[0].muscleGroupId")
                                .value(chest.getId())
                )
                .andExpect(
                        jsonPath("$.muscleGroups[0].muscleGroupName")
                                .value("Integration Chest")
                )
                .andExpect(
                        jsonPath("$.muscleGroups[0].role")
                                .value("PRIMARY")
                )
                .andExpect(
                        jsonPath("$.muscleGroups[1].muscleGroupId")
                                .value(triceps.getId())
                )
                .andExpect(
                        jsonPath("$.muscleGroups[1].muscleGroupName")
                                .value("Integration Triceps")
                )
                .andExpect(
                        jsonPath("$.muscleGroups[1].role")
                                .value("SECONDARY")
                );
    }

    @Test
    void updateExercise_shouldReplaceMuscleGroups()
            throws Exception {

        MuscleGroup chest = new MuscleGroup();
        chest.setName("Integration Update Chest");
        chest = muscleGroupRepository.save(chest);

        MuscleGroup back = new MuscleGroup();
        back.setName("Integration Update Back");
        back = muscleGroupRepository.save(back);

        Exercise exercise = new Exercise();
        exercise.setName("Exercise Before Muscle Update");
        exercise.setDescription("Before update");
        exercise.setCreatedAt(LocalDateTime.now());

        Exercise savedExercise =
                exerciseRepository.save(exercise);

        String token = createToken("exercise-muscle-update-user");

        String requestBody = """
                {
                    "name": "Exercise After Muscle Update",
                    "description": "After update",
                    "muscleGroups": [
                        {
                            "muscleGroupId": %d,
                            "role": "PRIMARY"
                        },
                        {
                            "muscleGroupId": %d,
                            "role": "SECONDARY"
                        }
                    ]
                }
                """.formatted(
                back.getId(),
                chest.getId()
        );

        mockMvc.perform(
                        put("/api/exercises/" + savedExercise.getId())
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
                                .value("Exercise After Muscle Update")
                )
                .andExpect(
                        jsonPath("$.muscleGroups.length()")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.muscleGroups[0].muscleGroupId")
                                .value(back.getId())
                )
                .andExpect(
                        jsonPath("$.muscleGroups[0].role")
                                .value("PRIMARY")
                )
                .andExpect(
                        jsonPath("$.muscleGroups[1].muscleGroupId")
                                .value(chest.getId())
                )
                .andExpect(
                        jsonPath("$.muscleGroups[1].role")
                                .value("SECONDARY")
                );
    }
}