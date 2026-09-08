package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.WorkoutResponse;
import com.gym.gym_api.dto.workout.WorkoutRequest;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.Workout;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private WorkoutTemplateRepository workoutTemplateRepository;

    private WorkoutService workoutService;

    @BeforeEach
    void setUp() {
        workoutService = new WorkoutService(workoutRepository, userRepository, exerciseRepository, workoutTemplateRepository);
    }

    @Test
    void getWorkoutById_shouldRejectWorkoutOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User otherUser = mock(User.class);
        when(otherUser.getId()).thenReturn(2L);

        Workout workout = mock(Workout.class);
        when(workout.getId()).thenReturn(10L);
        when(workout.getUser()).thenReturn(otherUser);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(authenticatedUser));

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));

        assertThrows(ResourceNotFoundException.class, () -> workoutService.getWorkoutById(10L, authentication));
    }

    @Test
    void getWorkoutById_shouldReturnWorkoutOwnedByAuthenticatedUser() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Workout workout = mock(Workout.class);
        when(workout.getId()).thenReturn(10L);
        when(workout.getUser()).thenReturn(user);
        when(workout.getName()).thenReturn("Push Day");
        when(workout.getWorkoutDate()).thenReturn(null);
        when(workout.getDurationSeconds()).thenReturn(null);
        when(workout.getStartedAt()).thenReturn(null);
        when(workout.getFinishedAt()).thenReturn(null);
        when(workout.getCreatedAt()).thenReturn(null);
        when(workout.isFinished()).thenReturn(false);
        when(workout.getExercises()).thenReturn(List.of());

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));

        WorkoutResponse response = workoutService.getWorkoutById(10L, authentication);

        assertEquals(10L, response.getId());
        assertEquals("Push Day", response.getName());
    }

    @Test
    void createWorkout_shouldRejectTemplateOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User templateOwner = mock(User.class);
        when(templateOwner.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(templateOwner);

        WorkoutRequest request = mock(WorkoutRequest.class);

        when(request.getWorkoutTemplateId()).thenReturn(3L);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(authenticatedUser));

        when(workoutTemplateRepository.findById(3L)).thenReturn(Optional.of(template));

        assertThrows(ResourceNotFoundException.class, () -> workoutService.createWorkout(request, authentication));
    }

    @Test
    void createWorkout_shouldCreateWorkoutWithOwnedTemplate() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(user);
        when(template.getName()).thenReturn("Push Day");

        WorkoutRequest request = mock(WorkoutRequest.class);

        when(request.getWorkoutTemplateId()).thenReturn(3L);

        when(request.getName()).thenReturn("Push Day");

        when(request.getWorkoutDate()).thenReturn(java.time.LocalDate.of(2026, 9, 8));

        when(request.getExercises()).thenReturn(List.of());

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        when(workoutTemplateRepository.findById(3L)).thenReturn(Optional.of(template));

        Workout savedWorkout = mock(Workout.class);

        when(savedWorkout.getId()).thenReturn(10L);
        when(savedWorkout.getUser()).thenReturn(user);
        when(savedWorkout.getWorkoutTemplate()).thenReturn(template);
        when(savedWorkout.getName()).thenReturn("Push Day");
        when(savedWorkout.getWorkoutDate()).thenReturn(java.time.LocalDate.of(2026, 9, 8));
        when(savedWorkout.getDurationSeconds()).thenReturn(null);
        when(savedWorkout.getStartedAt()).thenReturn(null);
        when(savedWorkout.getFinishedAt()).thenReturn(null);
        when(savedWorkout.getCreatedAt()).thenReturn(null);
        when(savedWorkout.isFinished()).thenReturn(false);
        when(savedWorkout.getExercises()).thenReturn(List.of());

        when(workoutRepository.save(org.mockito.ArgumentMatchers.any(Workout.class))).thenReturn(savedWorkout);

        WorkoutResponse response = workoutService.createWorkout(request, authentication);

        assertEquals(10L, response.getId());
        assertEquals("Push Day", response.getName());
        assertEquals(3L, response.getWorkoutTemplateId());
        assertEquals("Push Day", response.getWorkoutTemplateName());
    }

    @Test
    void finishWorkout_shouldCalculateDurationAndMarkWorkoutAsFinished() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(60);

        Workout workout = mock(Workout.class);

        when(workout.getId()).thenReturn(10L);
        when(workout.getUser()).thenReturn(user);
        when(workout.getStartedAt()).thenReturn(startedAt);
        when(workout.isFinished()).thenReturn(false);
        when(workout.getWorkoutTemplate()).thenReturn(null);
        when(workout.getName()).thenReturn("Push Day");
        when(workout.getWorkoutDate()).thenReturn(null);
        when(workout.getCreatedAt()).thenReturn(null);
        when(workout.getDurationSeconds()).thenReturn(3600);
        when(workout.getFinishedAt()).thenReturn(LocalDateTime.now());
        when(workout.getExercises()).thenReturn(List.of());

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));

        when(workoutRepository.save(any(Workout.class))).thenReturn(workout);

        WorkoutResponse response = workoutService.finishWorkout(10L, authentication);

        verify(workout).setFinished(true);
        verify(workout).setFinishedAt(any(LocalDateTime.class));
        verify(workout).setDurationSeconds(any(Integer.class));

        verify(workoutRepository).save(workout);

        assertEquals(10L, response.getId());
    }

    @Test
    void finishWorkout_shouldRejectAlreadyFinishedWorkout() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Workout workout = mock(Workout.class);
        when(workout.getUser()).thenReturn(user);
        when(workout.isFinished()).thenReturn(true);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        when(workoutRepository.findById(10L)).thenReturn(Optional.of(workout));

        assertThrows(IllegalArgumentException.class, () -> workoutService.finishWorkout(10L, authentication));
    }

    @Test
    void createWorkout_shouldCreateManualWorkoutWithoutTemplate() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutRequest request = mock(WorkoutRequest.class);

        when(request.getWorkoutTemplateId()).thenReturn(null);

        when(request.getName()).thenReturn("Quick Workout");

        when(request.getWorkoutDate()).thenReturn(java.time.LocalDate.of(2026, 9, 8));

        when(request.getExercises()).thenReturn(List.of());

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        Workout savedWorkout = mock(Workout.class);

        when(savedWorkout.getId()).thenReturn(11L);
        when(savedWorkout.getUser()).thenReturn(user);
        when(savedWorkout.getWorkoutTemplate()).thenReturn(null);
        when(savedWorkout.getName()).thenReturn("Quick Workout");
        when(savedWorkout.getWorkoutDate()).thenReturn(java.time.LocalDate.of(2026, 9, 8));
        when(savedWorkout.getDurationSeconds()).thenReturn(null);
        when(savedWorkout.getStartedAt()).thenReturn(null);
        when(savedWorkout.getFinishedAt()).thenReturn(null);
        when(savedWorkout.getCreatedAt()).thenReturn(null);
        when(savedWorkout.isFinished()).thenReturn(false);
        when(savedWorkout.getExercises()).thenReturn(List.of());

        when(workoutRepository.save(org.mockito.ArgumentMatchers.any(Workout.class))).thenReturn(savedWorkout);

        WorkoutResponse response = workoutService.createWorkout(request, authentication);

        assertEquals(11L, response.getId());
        assertEquals("Quick Workout", response.getName());
        assertEquals(null, response.getWorkoutTemplateId());
        assertEquals(null, response.getWorkoutTemplateName());
    }

    @Test
    void getWorkoutHistory_shouldReturnPaginatedWorkoutHistory() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Workout workout = mock(Workout.class);
        when(workout.getId()).thenReturn(10L);
        when(workout.getUser()).thenReturn(user);
        when(workout.getWorkoutTemplate()).thenReturn(null);
        when(workout.getName()).thenReturn("Push Day");
        when(workout.getWorkoutDate()).thenReturn(LocalDate.of(2026, 9, 8));
        when(workout.getDurationSeconds()).thenReturn(3600);
        when(workout.getStartedAt()).thenReturn(null);
        when(workout.getFinishedAt()).thenReturn(null);
        when(workout.getCreatedAt()).thenReturn(null);
        when(workout.isFinished()).thenReturn(true);
        when(workout.getExercises()).thenReturn(List.of());

        Page<Workout> workoutPage = new PageImpl<>(List.of(workout));

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("john");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));

        when(workoutRepository.findWorkoutHistory(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(3L), org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 9, 1)), org.mockito.ArgumentMatchers.eq(LocalDate.of(2026, 9, 30)), org.mockito.ArgumentMatchers.any(Pageable.class))).thenReturn(workoutPage);

        var result = workoutService.getWorkoutHistory(3L, 1L, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), 0, 10, authentication);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());

        assertEquals(10L, result.getContent().get(0).getId());
        assertEquals("Push Day", result.getContent().get(0).getName());
        assertEquals(3600, result.getContent().get(0).getDurationSeconds());
    }

    @Test
    void getWorkoutHistory_shouldRejectNegativePage() {

        User user = mock(User.class);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> workoutService.getWorkoutHistory(
                        null,
                        null,
                        null,
                        null,
                        -1,
                        10,
                        authentication
                )
        );
    }

    @Test
    void getWorkoutHistory_shouldRejectInvalidPageSize() {

        User user = mock(User.class);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> workoutService.getWorkoutHistory(
                        null,
                        null,
                        null,
                        null,
                        0,
                        101,
                        authentication
                )
        );
    }

    @Test
    void getWorkoutHistory_shouldRejectInvalidDateRange() {

        User user = mock(User.class);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(
                IllegalArgumentException.class,
                () -> workoutService.getWorkoutHistory(
                        null,
                        null,
                        java.time.LocalDate.of(2026, 9, 30),
                        java.time.LocalDate.of(2026, 9, 1),
                        0,
                        10,
                        authentication
                )
        );
    }
}