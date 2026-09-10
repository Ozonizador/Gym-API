package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.schedule.ScheduleRequest;
import com.gym.gym_api.dto.workout.schedule.ScheduleResponse;
import com.gym.gym_api.entity.Schedule;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ScheduleRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WorkoutTemplateRepository workoutTemplateRepository;

    private ScheduleService scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleService(
                scheduleRepository,
                userRepository,
                workoutTemplateRepository
        );
    }

    @Test
    void getScheduleById_shouldThrowWhenScheduleDoesNotExist() {

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(
                        mock(com.gym.gym_api.entity.User.class)
                ));

        when(scheduleRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.getScheduleById(
                        99L,
                        authentication
                )
        );
    }

    @Test
    void createSchedule_shouldRejectTemplateOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User templateOwner = mock(User.class);
        when(templateOwner.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(templateOwner);

        ScheduleRequest request = mock(ScheduleRequest.class);

        when(request.getWorkoutTemplateId())
                .thenReturn(3L);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(authenticatedUser));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.createSchedule(
                        request,
                        authentication
                )
        );
    }

    @Test
    void createSchedule_shouldCreateScheduleWithOwnedTemplate() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(user);
        when(template.getName()).thenReturn("Push Day");

        ScheduleRequest request = mock(ScheduleRequest.class);

        when(request.getWorkoutTemplateId())
                .thenReturn(3L);

        when(request.getScheduledDate())
                .thenReturn(LocalDate.of(2026, 9, 10));

        when(request.getScheduledTime())
                .thenReturn(LocalTime.of(18, 0));

        when(request.getNotes())
                .thenReturn("Evening workout");

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        Schedule savedSchedule = mock(Schedule.class);

        when(savedSchedule.getId()).thenReturn(10L);
        when(savedSchedule.getUser()).thenReturn(user);
        when(savedSchedule.getWorkoutTemplate()).thenReturn(template);
        when(savedSchedule.getScheduledDate())
                .thenReturn(LocalDate.of(2026, 9, 10));
        when(savedSchedule.getScheduledTime())
                .thenReturn(LocalTime.of(18, 0));
        when(savedSchedule.getNotes())
                .thenReturn("Evening workout");
        when(savedSchedule.getCreatedAt())
                .thenReturn(null);

        when(scheduleRepository.save(any(Schedule.class)))
                .thenReturn(savedSchedule);

        ScheduleResponse response =
                scheduleService.createSchedule(
                        request,
                        authentication
                );

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(3L, response.getWorkoutTemplateId());
        assertEquals("Push Day", response.getWorkoutTemplateName());
        assertEquals(
                LocalDate.of(2026, 9, 10),
                response.getScheduledDate()
        );
        assertEquals(
                LocalTime.of(18, 0),
                response.getScheduledTime()
        );
        assertEquals("Evening workout", response.getNotes());

        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void getScheduleById_shouldRejectScheduleOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User scheduleOwner = mock(User.class);
        when(scheduleOwner.getId()).thenReturn(2L);

        Schedule schedule = mock(Schedule.class);
        when(schedule.getId()).thenReturn(10L);
        when(schedule.getUser()).thenReturn(scheduleOwner);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(authenticatedUser));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.getScheduleById(
                        10L,
                        authentication
                )
        );
    }

    @Test
    void getScheduleById_shouldReturnScheduleOwnedByAuthenticatedUser() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getName()).thenReturn("Push Day");

        Schedule schedule = mock(Schedule.class);
        when(schedule.getId()).thenReturn(10L);
        when(schedule.getUser()).thenReturn(user);
        when(schedule.getWorkoutTemplate()).thenReturn(template);
        when(schedule.getScheduledDate())
                .thenReturn(LocalDate.of(2026, 9, 10));
        when(schedule.getScheduledTime())
                .thenReturn(LocalTime.of(18, 0));
        when(schedule.getNotes())
                .thenReturn("Evening workout");
        when(schedule.getCreatedAt())
                .thenReturn(null);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        ScheduleResponse response =
                scheduleService.getScheduleById(
                        10L,
                        authentication
                );

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals(3L, response.getWorkoutTemplateId());
        assertEquals("Push Day", response.getWorkoutTemplateName());
        assertEquals(
                LocalDate.of(2026, 9, 10),
                response.getScheduledDate()
        );
        assertEquals(
                LocalTime.of(18, 0),
                response.getScheduledTime()
        );
        assertEquals("Evening workout", response.getNotes());
    }

    @Test
    void deleteSchedule_shouldDeleteScheduleOwnedByAuthenticatedUser() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Schedule schedule = mock(Schedule.class);
        when(schedule.getUser()).thenReturn(user);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        scheduleService.deleteSchedule(
                10L,
                authentication
        );

        verify(scheduleRepository).delete(schedule);
    }

    @Test
    void deleteSchedule_shouldRejectScheduleOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User scheduleOwner = mock(User.class);
        when(scheduleOwner.getId()).thenReturn(2L);

        Schedule schedule = mock(Schedule.class);
        when(schedule.getUser()).thenReturn(scheduleOwner);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(authenticatedUser));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.deleteSchedule(
                        10L,
                        authentication
                )
        );
    }

    @Test
    void getAllSchedules_shouldReturnOnlyAuthenticatedUsersSchedules() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getName()).thenReturn("Push Day");

        Schedule ownSchedule = mock(Schedule.class);
        when(ownSchedule.getId()).thenReturn(10L);
        when(ownSchedule.getUser()).thenReturn(user);
        when(ownSchedule.getWorkoutTemplate()).thenReturn(template);
        when(ownSchedule.getScheduledDate())
                .thenReturn(LocalDate.of(2026, 9, 10));
        when(ownSchedule.getScheduledTime())
                .thenReturn(LocalTime.of(18, 0));
        when(ownSchedule.getNotes())
                .thenReturn("Evening workout");
        when(ownSchedule.getCreatedAt())
                .thenReturn(null);

        Schedule otherSchedule = mock(Schedule.class);
        when(otherSchedule.getUser()).thenReturn(otherUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findAll())
                .thenReturn(java.util.List.of(
                        ownSchedule,
                        otherSchedule
                ));

        var result =
                scheduleService.getAllSchedules(authentication);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals(3L, result.get(0).getWorkoutTemplateId());
        assertEquals(
                "Push Day",
                result.get(0).getWorkoutTemplateName()
        );
    }

    @Test
    void createSchedule_shouldRejectMissingTemplate() {

        User user = mock(User.class);

        ScheduleRequest request = mock(ScheduleRequest.class);

        when(request.getWorkoutTemplateId())
                .thenReturn(999L);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.createSchedule(
                        request,
                        authentication
                )
        );
    }

    @Test
    void updateSchedule_shouldUpdateScheduleSuccessfully() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(user);
        when(template.getName()).thenReturn("Updated Template");

        Schedule schedule = mock(Schedule.class);
        when(schedule.getId()).thenReturn(10L);
        when(schedule.getUser()).thenReturn(user);
        when(schedule.getWorkoutTemplate()).thenReturn(template);
        when(schedule.getScheduledDate())
                .thenReturn(LocalDate.of(2026, 9, 15));
        when(schedule.getScheduledTime())
                .thenReturn(LocalTime.of(19, 0));
        when(schedule.getNotes())
                .thenReturn("Updated notes");
        when(schedule.getCreatedAt())
                .thenReturn(null);

        ScheduleRequest request = mock(ScheduleRequest.class);

        when(request.getWorkoutTemplateId())
                .thenReturn(3L);
        when(request.getScheduledDate())
                .thenReturn(LocalDate.of(2026, 9, 15));
        when(request.getScheduledTime())
                .thenReturn(LocalTime.of(19, 0));
        when(request.getNotes())
                .thenReturn("Updated notes");

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        when(scheduleRepository.save(schedule))
                .thenReturn(schedule);

        ScheduleResponse response =
                scheduleService.updateSchedule(
                        10L,
                        request,
                        authentication
                );

        verify(schedule).setWorkoutTemplate(template);
        verify(schedule).setScheduledDate(
                LocalDate.of(2026, 9, 15)
        );
        verify(schedule).setScheduledTime(
                LocalTime.of(19, 0)
        );
        verify(schedule).setNotes("Updated notes");

        verify(scheduleRepository).save(schedule);

        assertEquals(10L, response.getId());
        assertEquals(
                "Updated Template",
                response.getWorkoutTemplateName()
        );
    }

    @Test
    void updateSchedule_shouldRejectMissingTemplate() {

        User user = mock(User.class);

        Schedule schedule = mock(Schedule.class);
        when(schedule.getUser()).thenReturn(user);

        ScheduleRequest request = mock(ScheduleRequest.class);

        when(request.getWorkoutTemplateId())
                .thenReturn(999L);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        when(workoutTemplateRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.updateSchedule(
                        10L,
                        request,
                        authentication
                )
        );
    }

    @Test
    void updateSchedule_shouldRejectTemplateOwnedByAnotherUser() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        User templateOwner = mock(User.class);
        when(templateOwner.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(templateOwner);

        Schedule schedule = mock(Schedule.class);
        when(schedule.getUser()).thenReturn(user);

        ScheduleRequest request = mock(ScheduleRequest.class);

        when(request.getWorkoutTemplateId())
                .thenReturn(3L);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findById(10L))
                .thenReturn(Optional.of(schedule));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.updateSchedule(
                        10L,
                        request,
                        authentication
                )
        );
    }

    @Test
    void deleteSchedule_shouldRejectMissingSchedule() {

        User user = mock(User.class);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(scheduleRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> scheduleService.deleteSchedule(
                        999L,
                        authentication
                )
        );
    }
}