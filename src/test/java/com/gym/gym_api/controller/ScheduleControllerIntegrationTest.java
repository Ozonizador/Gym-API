package com.gym.gym_api.controller;

import com.gym.gym_api.entity.Schedule;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.repository.ScheduleRepository;
import com.gym.gym_api.repository.UserRepository;
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
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ScheduleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void getAllSchedules_shouldReturn401WithoutAuthentication()
            throws Exception {

        mockMvc.perform(
                        get("/api/schedules")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllSchedules_shouldReturnOnlyAuthenticatedUsersSchedules()
            throws Exception {

        User user = createUser(
                "schedule-list-user",
                "schedule-list-user@example.com"
        );

        User otherUser = createUser(
                "schedule-list-other",
                "schedule-list-other@example.com"
        );

        WorkoutTemplate userTemplate =
                createTemplate(user, "User Push Template");

        WorkoutTemplate otherTemplate =
                createTemplate(otherUser, "Other Push Template");

        Schedule ownSchedule =
                createSchedule(
                        user,
                        userTemplate,
                        LocalDate.of(2099, 9, 10),
                        LocalTime.of(18, 0),
                        "My workout"
                );

        createSchedule(
                otherUser,
                otherTemplate,
                LocalDate.of(2099, 9, 11),
                LocalTime.of(19, 0),
                "Other workout"
        );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        mockMvc.perform(
                        get("/api/schedules")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(
                        jsonPath("$[*].id")
                                .value(
                                        org.hamcrest.Matchers.hasItem(
                                                ownSchedule.getId().intValue()
                                        )
                                )
                );
    }

    @Test
    void getScheduleById_shouldReturnScheduleOwnedByAuthenticatedUser()
            throws Exception {

        User user = createUser(
                "schedule-get-user",
                "schedule-get-user@example.com"
        );

        WorkoutTemplate template =
                createTemplate(user, "Get Push Template");

        Schedule schedule =
                createSchedule(
                        user,
                        template,
                        LocalDate.of(2099, 9, 15),
                        LocalTime.of(18, 30),
                        "Evening workout"
                );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        mockMvc.perform(
                        get("/api/schedules/" + schedule.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(schedule.getId())
                )
                .andExpect(
                        jsonPath("$.userId")
                                .value(user.getId())
                )
                .andExpect(
                        jsonPath("$.workoutTemplateId")
                                .value(template.getId())
                )
                .andExpect(
                        jsonPath("$.workoutTemplateName")
                                .value("Get Push Template")
                )
                .andExpect(
                        jsonPath("$.scheduledDate")
                                .value("2099-09-15")
                )
                .andExpect(
                        jsonPath("$.scheduledTime")
                                .value("18:30:00")
                )
                .andExpect(
                        jsonPath("$.notes")
                                .value("Evening workout")
                );
    }

    @Test
    void getScheduleById_shouldReturn404WhenScheduleBelongsToAnotherUser()
            throws Exception {

        User owner = createUser(
                "schedule-owner",
                "schedule-owner@example.com"
        );

        User otherUser = createUser(
                "schedule-reader",
                "schedule-reader@example.com"
        );

        WorkoutTemplate template =
                createTemplate(owner, "Private Template");

        Schedule schedule =
                createSchedule(
                        owner,
                        template,
                        LocalDate.of(2099, 9, 16),
                        LocalTime.of(18, 0),
                        "Private"
                );

        String token = jwtService.generateToken(
                otherUser.getUsername()
        );

        mockMvc.perform(
                        get("/api/schedules/" + schedule.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getScheduleById_shouldReturn404WhenScheduleDoesNotExist()
            throws Exception {

        User user = createUser(
                "schedule-missing-user",
                "schedule-missing-user@example.com"
        );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        mockMvc.perform(
                        get("/api/schedules/999999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createSchedule_shouldCreateScheduleSuccessfully()
            throws Exception {

        User user = createUser(
                "schedule-create-user",
                "schedule-create-user@example.com"
        );

        WorkoutTemplate template =
                createTemplate(user, "Create Push Template");

        String token = jwtService.generateToken(
                user.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2099-09-20",
                    "scheduledTime": "18:00:00",
                    "workoutTemplateId": %d,
                    "notes": "Evening push workout"
                }
                """.formatted(template.getId());

        mockMvc.perform(
                        post("/api/schedules")
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
                        jsonPath("$.userId")
                                .value(user.getId())
                )
                .andExpect(
                        jsonPath("$.workoutTemplateId")
                                .value(template.getId())
                )
                .andExpect(
                        jsonPath("$.workoutTemplateName")
                                .value("Create Push Template")
                )
                .andExpect(
                        jsonPath("$.scheduledDate")
                                .value("2099-09-20")
                )
                .andExpect(
                        jsonPath("$.scheduledTime")
                                .value("18:00:00")
                )
                .andExpect(
                        jsonPath("$.notes")
                                .value("Evening push workout")
                );

        Schedule savedSchedule =
                scheduleRepository.findAll()
                        .stream()
                        .filter(schedule ->
                                user.getId().equals(
                                        schedule.getUser().getId()
                                )
                                        && template.getId().equals(
                                        schedule.getWorkoutTemplate().getId()
                                )
                        )
                        .findFirst()
                        .orElseThrow();

        assertEquals(
                LocalDate.of(2099, 9, 20),
                savedSchedule.getScheduledDate()
        );

        assertEquals(
                LocalTime.of(18, 0),
                savedSchedule.getScheduledTime()
        );

        assertEquals(
                "Evening push workout",
                savedSchedule.getNotes()
        );
    }

    @Test
    void createSchedule_shouldReturn404WhenTemplateDoesNotExist()
            throws Exception {

        User user = createUser(
                "schedule-no-template-user",
                "schedule-no-template-user@example.com"
        );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2099-09-21",
                    "scheduledTime": "18:00:00",
                    "workoutTemplateId": 999999,
                    "notes": "Missing template"
                }
                """;

        mockMvc.perform(
                        post("/api/schedules")
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
    void createSchedule_shouldReturn404WhenTemplateBelongsToAnotherUser()
            throws Exception {

        User owner = createUser(
                "schedule-template-owner",
                "schedule-template-owner@example.com"
        );

        User otherUser = createUser(
                "schedule-template-other",
                "schedule-template-other@example.com"
        );

        WorkoutTemplate template =
                createTemplate(owner, "Private Schedule Template");

        String token = jwtService.generateToken(
                otherUser.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2099-09-22",
                    "scheduledTime": "18:00:00",
                    "workoutTemplateId": %d,
                    "notes": "Should fail"
                }
                """.formatted(template.getId());

        mockMvc.perform(
                        post("/api/schedules")
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
    void createSchedule_shouldReturn400WhenScheduledDateIsInThePast()
            throws Exception {

        User user = createUser(
                "schedule-validation-user",
                "schedule-validation-user@example.com"
        );

        WorkoutTemplate template =
                createTemplate(user, "Validation Template");

        String token = jwtService.generateToken(
                user.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2020-01-01",
                    "scheduledTime": "18:00:00",
                    "workoutTemplateId": %d,
                    "notes": "Past date"
                }
                """.formatted(template.getId());

        mockMvc.perform(
                        post("/api/schedules")
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
    void updateSchedule_shouldUpdateOwnedScheduleSuccessfully()
            throws Exception {

        User user = createUser(
                "schedule-update-user",
                "schedule-update-user@example.com"
        );

        WorkoutTemplate originalTemplate =
                createTemplate(user, "Original Template");

        WorkoutTemplate updatedTemplate =
                createTemplate(user, "Updated Template");

        Schedule schedule =
                createSchedule(
                        user,
                        originalTemplate,
                        LocalDate.of(2099, 9, 23),
                        LocalTime.of(18, 0),
                        "Original notes"
                );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2099-09-24",
                    "scheduledTime": "19:30:00",
                    "workoutTemplateId": %d,
                    "notes": "Updated notes"
                }
                """.formatted(updatedTemplate.getId());

        mockMvc.perform(
                        put("/api/schedules/" + schedule.getId())
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
                        jsonPath("$.workoutTemplateId")
                                .value(updatedTemplate.getId())
                )
                .andExpect(
                        jsonPath("$.workoutTemplateName")
                                .value("Updated Template")
                )
                .andExpect(
                        jsonPath("$.scheduledDate")
                                .value("2099-09-24")
                )
                .andExpect(
                        jsonPath("$.scheduledTime")
                                .value("19:30:00")
                )
                .andExpect(
                        jsonPath("$.notes")
                                .value("Updated notes")
                );

        Schedule updatedSchedule =
                scheduleRepository.findById(
                        schedule.getId()
                ).orElseThrow();

        assertEquals(
                updatedTemplate.getId(),
                updatedSchedule.getWorkoutTemplate().getId()
        );

        assertEquals(
                LocalDate.of(2099, 9, 24),
                updatedSchedule.getScheduledDate()
        );

        assertEquals(
                LocalTime.of(19, 30),
                updatedSchedule.getScheduledTime()
        );

        assertEquals(
                "Updated notes",
                updatedSchedule.getNotes()
        );
    }

    @Test
    void updateSchedule_shouldReturn404WhenScheduleBelongsToAnotherUser()
            throws Exception {

        User owner = createUser(
                "schedule-update-owner",
                "schedule-update-owner@example.com"
        );

        User otherUser = createUser(
                "schedule-update-other",
                "schedule-update-other@example.com"
        );

        WorkoutTemplate template =
                createTemplate(owner, "Owner Template");

        Schedule schedule =
                createSchedule(
                        owner,
                        template,
                        LocalDate.of(2099, 9, 25),
                        LocalTime.of(18, 0),
                        "Owner schedule"
                );

        String token = jwtService.generateToken(
                otherUser.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2099-09-26",
                    "scheduledTime": "19:00:00",
                    "workoutTemplateId": %d,
                    "notes": "Unauthorized update"
                }
                """.formatted(template.getId());

        mockMvc.perform(
                        put("/api/schedules/" + schedule.getId())
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
    void updateSchedule_shouldReturn404WhenTemplateBelongsToAnotherUser()
            throws Exception {

        User user = createUser(
                "schedule-update-template-user",
                "schedule-update-template-user@example.com"
        );

        User templateOwner = createUser(
                "schedule-update-template-owner",
                "schedule-update-template-owner@example.com"
        );

        WorkoutTemplate ownTemplate =
                createTemplate(user, "Own Template");

        WorkoutTemplate foreignTemplate =
                createTemplate(
                        templateOwner,
                        "Foreign Template"
                );

        Schedule schedule =
                createSchedule(
                        user,
                        ownTemplate,
                        LocalDate.of(2099, 9, 27),
                        LocalTime.of(18, 0),
                        "Original"
                );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        String requestBody = """
                {
                    "scheduledDate": "2099-09-28",
                    "scheduledTime": "19:00:00",
                    "workoutTemplateId": %d,
                    "notes": "Should fail"
                }
                """.formatted(foreignTemplate.getId());

        mockMvc.perform(
                        put("/api/schedules/" + schedule.getId())
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
    void deleteSchedule_shouldDeleteOwnedSchedule()
            throws Exception {

        User user = createUser(
                "schedule-delete-user",
                "schedule-delete-user@example.com"
        );

        WorkoutTemplate template =
                createTemplate(user, "Delete Template");

        Schedule schedule =
                createSchedule(
                        user,
                        template,
                        LocalDate.of(2099, 9, 29),
                        LocalTime.of(18, 0),
                        "Delete me"
                );

        String token = jwtService.generateToken(
                user.getUsername()
        );

        mockMvc.perform(
                        delete("/api/schedules/" + schedule.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());

        assertTrue(
                scheduleRepository.findById(
                        schedule.getId()
                ).isEmpty()
        );
    }

    @Test
    void deleteSchedule_shouldReturn404WhenScheduleBelongsToAnotherUser()
            throws Exception {

        User owner = createUser(
                "schedule-delete-owner",
                "schedule-delete-owner@example.com"
        );

        User otherUser = createUser(
                "schedule-delete-other",
                "schedule-delete-other@example.com"
        );

        WorkoutTemplate template =
                createTemplate(owner, "Protected Template");

        Schedule schedule =
                createSchedule(
                        owner,
                        template,
                        LocalDate.of(2099, 9, 30),
                        LocalTime.of(18, 0),
                        "Protected"
                );

        String token = jwtService.generateToken(
                otherUser.getUsername()
        );

        mockMvc.perform(
                        delete("/api/schedules/" + schedule.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

        assertTrue(
                scheduleRepository.findById(
                        schedule.getId()
                ).isPresent()
        );
    }

    private User createUser(
            String username,
            String email
    ) {

        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode("password123")
        );

        return userRepository.save(user);
    }

    private WorkoutTemplate createTemplate(
            User user,
            String name
    ) {

        WorkoutTemplate template =
                new WorkoutTemplate();

        template.setUser(user);
        template.setName(name);
        template.setDescription("Schedule integration test");
        template.setCreatedAt(LocalDateTime.now());

        return workoutTemplateRepository.save(template);
    }

    private Schedule createSchedule(
            User user,
            WorkoutTemplate template,
            LocalDate date,
            LocalTime time,
            String notes
    ) {

        Schedule schedule = new Schedule();

        schedule.setUser(user);
        schedule.setWorkoutTemplate(template);
        schedule.setScheduledDate(date);
        schedule.setScheduledTime(time);
        schedule.setNotes(notes);
        schedule.setCreatedAt(LocalDateTime.now());

        return scheduleRepository.save(schedule);
    }
}