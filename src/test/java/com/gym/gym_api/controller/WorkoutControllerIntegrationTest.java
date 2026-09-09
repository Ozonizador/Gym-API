package com.gym.gym_api.controller;

import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.Workout;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import com.gym.gym_api.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkoutControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Test
    void getWorkoutHistory_shouldReturn401WithoutAuthentication() throws Exception {

        mockMvc.perform(get("/api/workouts/history").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getWorkoutHistory_shouldReturn200WithValidJwt() throws Exception {

        User user = new User();
        user.setUsername("john");
        user.setEmail("john-history@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));

        user = userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername());

        mockMvc.perform(get("/api/workouts/history").header("Authorization", "Bearer " + token)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test
    void createWorkout_shouldCreateWorkoutForAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("john-create");
        user.setEmail("john-create@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getUsername());

        String requestBody = """
                {
                    "name": "Push Day",
                    "workoutDate": "2026-09-09",
                    "exercises": []
                }
                """;

        mockMvc.perform(post("/api/workouts").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

        List<Workout> workouts = workoutRepository.findAll();

        Workout savedWorkout = workouts.stream()
                .filter(workout -> "Push Day".equals(workout.getName()) &&
                        savedUser.getId().equals(workout.getUser().getId())).findFirst()
                .orElseThrow();

        assertEquals("Push Day", savedWorkout.getName());

        assertEquals(savedUser.getUsername(), savedWorkout.getUser().getUsername());

        assertEquals(savedUser.getId(), savedWorkout.getUser().getId());

        assertTrue(savedWorkout.getExercises().isEmpty());
    }

    @Test
    void createWorkout_shouldReturn401WithoutAuthentication() throws Exception {

        String requestBody = """
                {
                    "name": "Push Day",
                    "workoutDate": "2026-09-09",
                    "exercises": []
                }
                """;

        mockMvc.perform(
                post("/api/workouts").contentType(MediaType.APPLICATION_JSON).content(requestBody)
                        .accept(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
    }

    @Test
    void createWorkout_shouldReturn400ForBlankName() throws Exception {

        User user = new User();
        user.setUsername("john-validation");
        user.setEmail("john-validation@example.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getUsername());

        String requestBody = """
                {
                    "name": "",
                    "workoutDate": "2026-09-09",
                    "exercises": []
                }
                """;

        mockMvc.perform(post("/api/workouts").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(requestBody)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void createWorkout_shouldReturn400WhenWorkoutDateIsMissing() throws Exception {

        User user = new User();
        user.setUsername("john-date-validation");
        user.setEmail("john-date-validation@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Push Day",
                    "exercises": []
                }
                """;

        mockMvc.perform(
                        post("/api/workouts")
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
    void createWorkout_shouldReturn400WhenNameIsTooLong() throws Exception {

        User user = new User();
        user.setUsername("john-name-validation");
        user.setEmail("john-name-validation@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        String longName = "A".repeat(101);

        String requestBody = """
                {
                    "name": "%s",
                    "workoutDate": "2026-09-09",
                    "exercises": []
                }
                """.formatted(longName);

        mockMvc.perform(
                        post("/api/workouts")
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
    void getWorkoutHistory_shouldReturn409ForNegativePage() throws Exception {

        User user = new User();
        user.setUsername("john-page-validation");
        user.setEmail("john-page-validation@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workouts/history")
                                .param("page", "-1")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void getWorkoutHistory_shouldReturn409ForInvalidPageSize() throws Exception {

        User user = new User();
        user.setUsername("john-size-validation");
        user.setEmail("john-size-validation@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workouts/history")
                                .param("size", "101")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void getWorkoutHistory_shouldReturn409WhenFromDateIsAfterToDate() throws Exception {

        User user = new User();
        user.setUsername("john-date-range");
        user.setEmail("john-date-range@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workouts/history")
                                .param("from", "2026-09-10")
                                .param("to", "2026-09-09")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void getWorkoutById_shouldReturn404WhenWorkoutBelongsToAnotherUser() throws Exception {

        User owner = new User();
        owner.setUsername("workout-owner");
        owner.setEmail("workout-owner@example.com");
        owner.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOwner = userRepository.save(owner);

        User otherUser = new User();
        otherUser.setUsername("other-user");
        otherUser.setEmail("other-user@example.com");
        otherUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOtherUser = userRepository.save(otherUser);

        Workout workout = new Workout();
        workout.setUser(savedOwner);
        workout.setName("Private Workout");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(LocalDateTime.now());
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout = workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedOtherUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workouts/" + savedWorkout.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getWorkoutById_shouldReturnWorkoutOwnedByAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("workout-reader");
        user.setEmail("workout-reader@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        Workout workout = new Workout();
        workout.setUser(savedUser);
        workout.setName("Leg Day");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(LocalDateTime.now());
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout = workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workouts/" + savedWorkout.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void createWorkout_shouldReturn404WhenTemplateBelongsToAnotherUser() throws Exception {

        User templateOwner = new User();
        templateOwner.setUsername("template-owner");
        templateOwner.setEmail("template-owner@example.com");
        templateOwner.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedTemplateOwner =
                userRepository.save(templateOwner);

        User otherUser = new User();
        otherUser.setUsername("workout-creator");
        otherUser.setEmail("workout-creator@example.com");
        otherUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOtherUser =
                userRepository.save(otherUser);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedTemplateOwner);
        template.setName("Private Template");
        template.setDescription("Owner only");
        template.setCreatedAt(LocalDateTime.now());
        template.setExercises(new ArrayList<>());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedOtherUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Unauthorized Workout",
                    "workoutDate": "2026-09-09",
                    "workoutTemplateId": %d,
                    "exercises": []
                }
                """.formatted(savedTemplate.getId());

        mockMvc.perform(
                        post("/api/workouts")
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
    void createWorkout_shouldCreateWorkoutUsingOwnedTemplate() throws Exception {

        User user = new User();
        user.setUsername("template-workout-user");
        user.setEmail("template-workout-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        WorkoutTemplate template = new WorkoutTemplate();
        template.setUser(savedUser);
        template.setName("Push Template");
        template.setDescription("Chest and shoulders");
        template.setCreatedAt(LocalDateTime.now());
        template.setExercises(new ArrayList<>());

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Push Workout",
                    "workoutDate": "2026-09-09",
                    "workoutTemplateId": %d,
                    "exercises": []
                }
                """.formatted(savedTemplate.getId());

        mockMvc.perform(
                        post("/api/workouts")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated());

        List<Workout> workouts =
                workoutRepository.findAll();

        Workout savedWorkout = workouts.stream()
                .filter(workout ->
                        "Push Workout".equals(workout.getName())
                                && savedUser.getId().equals(
                                workout.getUser().getId()
                        )
                )
                .findFirst()
                .orElseThrow();

        assertEquals(
                "Push Workout",
                savedWorkout.getName()
        );

        assertEquals(
                savedUser.getId(),
                savedWorkout.getUser().getId()
        );

        assertEquals(
                savedTemplate.getId(),
                savedWorkout.getWorkoutTemplate().getId()
        );
    }

    @Test
    void getWorkoutById_shouldReturn404WhenWorkoutDoesNotExist() throws Exception {

        User user = new User();
        user.setUsername("missing-workout-user");
        user.setEmail("missing-workout-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/workouts/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateWorkout_shouldUpdateWorkoutOwnedByAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("workout-update-user");
        user.setEmail("workout-update-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        Workout workout = new Workout();
        workout.setUser(savedUser);
        workout.setName("Old Name");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(LocalDateTime.now());
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout = workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Updated Push Day",
                    "workoutDate": "2026-09-10",
                    "exercises": []
                }
                """;

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/workouts/" + savedWorkout.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        Workout updatedWorkout =
                workoutRepository.findById(savedWorkout.getId())
                        .orElseThrow();

        assertEquals(
                "Updated Push Day",
                updatedWorkout.getName()
        );

        assertEquals(
                LocalDate.of(2026, 9, 10),
                updatedWorkout.getWorkoutDate()
        );

        assertEquals(
                savedUser.getId(),
                updatedWorkout.getUser().getId()
        );

        assertTrue(
                updatedWorkout.getExercises().isEmpty()
        );
    }

    @Test
    void updateWorkout_shouldReturn404WhenWorkoutBelongsToAnotherUser() throws Exception {

        User owner = new User();
        owner.setUsername("update-owner");
        owner.setEmail("update-owner@example.com");
        owner.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOwner = userRepository.save(owner);

        User otherUser = new User();
        otherUser.setUsername("update-other-user");
        otherUser.setEmail("update-other-user@example.com");
        otherUser.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedOtherUser = userRepository.save(otherUser);

        Workout workout = new Workout();
        workout.setUser(savedOwner);
        workout.setName("Original Workout");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(LocalDateTime.now());
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout = workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedOtherUser.getUsername()
        );

        String requestBody = """
                {
                    "name": "Unauthorized Update",
                    "workoutDate": "2026-09-10",
                    "exercises": []
                }
                """;

        mockMvc.perform(
                        put("/api/workouts/" + savedWorkout.getId())
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
    void deleteWorkout_shouldDeleteWorkoutOwnedByAuthenticatedUser() throws Exception {

        User user = new User();
        user.setUsername("delete-workout-user");
        user.setEmail("delete-workout-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        Workout workout = new Workout();
        workout.setUser(savedUser);
        workout.setName("Workout To Delete");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(LocalDateTime.now());
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout = workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/workouts/" + savedWorkout.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        assertTrue(
                workoutRepository.findById(
                        savedWorkout.getId()
                ).isEmpty()
        );
    }

    @Test
    void finishWorkout_shouldFinishWorkoutSuccessfully() throws Exception {

        User user = new User();
        user.setUsername("finish-workout-user");
        user.setEmail("finish-workout-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        Workout workout = new Workout();
        workout.setUser(savedUser);
        workout.setName("Workout To Finish");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(LocalDateTime.now().minusMinutes(30));
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout =
                workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        patch(
                                "/api/workouts/"
                                        + savedWorkout.getId()
                                        + "/finish"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());

        Workout finishedWorkout =
                workoutRepository.findById(
                        savedWorkout.getId()
                ).orElseThrow();

        assertTrue(finishedWorkout.isFinished());

        assertEquals(
                savedWorkout.getUser().getId(),
                finishedWorkout.getUser().getId()
        );

        assertTrue(
                finishedWorkout.getFinishedAt() != null
        );

        assertTrue(
                finishedWorkout.getDurationSeconds() != null
        );

        assertTrue(
                finishedWorkout.getDurationSeconds() >= 1
        );
    }

    @Test
    void finishWorkout_shouldReturn409WhenWorkoutIsAlreadyFinished() throws Exception {

        User user = new User();
        user.setUsername("already-finished-user");
        user.setEmail("already-finished-user@example.com");
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        User savedUser = userRepository.save(user);

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(30);
        LocalDateTime finishedAt = LocalDateTime.now().minusMinutes(5);

        Workout workout = new Workout();
        workout.setUser(savedUser);
        workout.setName("Already Finished Workout");
        workout.setWorkoutDate(LocalDate.of(2026, 9, 9));
        workout.setStartedAt(startedAt);
        workout.setFinishedAt(finishedAt);
        workout.setFinished(true);
        workout.setDurationSeconds(1500);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setExercises(new ArrayList<>());

        Workout savedWorkout = workoutRepository.save(workout);

        String token = jwtService.generateToken(
                savedUser.getUsername()
        );

        mockMvc.perform(
                        patch(
                                "/api/workouts/"
                                        + savedWorkout.getId()
                                        + "/finish"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict());
    }
}