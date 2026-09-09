package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.template.WorkoutTemplateExerciseRequest;
import com.gym.gym_api.dto.workout.template.WorkoutTemplateRequest;
import com.gym.gym_api.dto.workout.template.WorkoutTemplateResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutTemplateServiceTest {

    @Mock
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    private WorkoutTemplateService workoutTemplateService;

    @BeforeEach
    void setUp() {
        workoutTemplateService = new WorkoutTemplateService(
                workoutTemplateRepository,
                userRepository,
                exerciseRepository
        );
    }

    @Test
    void getTemplateById_shouldRejectTemplateOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User templateOwner = mock(User.class);
        when(templateOwner.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(templateOwner);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(authenticatedUser));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutTemplateService.getTemplateById(
                        3L,
                        authentication
                )
        );
    }

    @Test
    void getTemplateById_shouldReturnTemplateOwnedByAuthenticatedUser() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(user);
        when(template.getName()).thenReturn("Push Day");
        when(template.getDescription()).thenReturn("Chest and triceps");
        when(template.getCreatedAt()).thenReturn(null);
        when(template.getExercises()).thenReturn(java.util.List.of());

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        WorkoutTemplateResponse response =
                workoutTemplateService.getTemplateById(
                        3L,
                        authentication
                );

        assertEquals(3L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("Push Day", response.getName());
        assertEquals("Chest and triceps", response.getDescription());
    }

    @Test
    void createTemplate_shouldCreateTemplateSuccessfully() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Exercise exercise = mock(Exercise.class);

        WorkoutTemplateRequest request =
                mock(WorkoutTemplateRequest.class);

        WorkoutTemplateExerciseRequest exerciseRequest =
                mock(WorkoutTemplateExerciseRequest.class);

        when(request.getName())
                .thenReturn("Push Day");

        when(request.getDescription())
                .thenReturn("Chest and triceps");

        when(request.getExercises())
                .thenReturn(List.of(exerciseRequest));

        when(exerciseRequest.getExerciseId())
                .thenReturn(5L);

        when(exerciseRequest.getPosition())
                .thenReturn(1);

        when(exerciseRequest.getTargetSets())
                .thenReturn(3);

        when(exerciseRequest.getTargetReps())
                .thenReturn(10);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(exerciseRepository.findById(5L))
                .thenReturn(Optional.of(exercise));

        WorkoutTemplate savedTemplate =
                mock(WorkoutTemplate.class);

        when(savedTemplate.getId())
                .thenReturn(3L);

        when(savedTemplate.getUser())
                .thenReturn(user);

        when(savedTemplate.getName())
                .thenReturn("Push Day");

        when(savedTemplate.getDescription())
                .thenReturn("Chest and triceps");

        when(savedTemplate.getCreatedAt())
                .thenReturn(null);

        when(savedTemplate.getExercises())
                .thenReturn(List.of());

        when(workoutTemplateRepository.save(
                any(WorkoutTemplate.class)
        )).thenReturn(savedTemplate);

        WorkoutTemplateResponse response =
                workoutTemplateService.createTemplate(
                        request,
                        authentication
                );

        assertEquals(3L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("Push Day", response.getName());
        assertEquals(
                "Chest and triceps",
                response.getDescription()
        );

        verify(workoutTemplateRepository)
                .save(any(WorkoutTemplate.class));
    }

    @Test
    void createTemplate_shouldRejectMissingExercise() {

        WorkoutTemplateRequest request =
                mock(WorkoutTemplateRequest.class);

        WorkoutTemplateExerciseRequest exerciseRequest =
                mock(WorkoutTemplateExerciseRequest.class);

        when(request.getName())
                .thenReturn("Push Day");

        when(request.getExercises())
                .thenReturn(List.of(exerciseRequest));

        when(exerciseRequest.getExerciseId())
                .thenReturn(999L);

        when(exerciseRepository.findById(999L))
                .thenReturn(Optional.empty());

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        User user = mock(User.class);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutTemplateService.createTemplate(
                        request,
                        authentication
                )
        );
    }

    @Test
    void createTemplate_shouldCreateTemplateWithMultipleExercises() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Exercise benchPress = mock(Exercise.class);
        Exercise shoulderPress = mock(Exercise.class);

        WorkoutTemplateRequest request =
                mock(WorkoutTemplateRequest.class);

        WorkoutTemplateExerciseRequest benchRequest =
                mock(WorkoutTemplateExerciseRequest.class);

        WorkoutTemplateExerciseRequest shoulderRequest =
                mock(WorkoutTemplateExerciseRequest.class);

        when(request.getName())
                .thenReturn("Push Day");

        when(request.getDescription())
                .thenReturn("Chest and shoulders");

        when(request.getExercises())
                .thenReturn(List.of(
                        benchRequest,
                        shoulderRequest
                ));

        when(benchRequest.getExerciseId())
                .thenReturn(1L);

        when(benchRequest.getPosition())
                .thenReturn(1);

        when(benchRequest.getTargetSets())
                .thenReturn(3);

        when(benchRequest.getTargetReps())
                .thenReturn(10);

        when(shoulderRequest.getExerciseId())
                .thenReturn(2L);

        when(shoulderRequest.getPosition())
                .thenReturn(2);

        when(shoulderRequest.getTargetSets())
                .thenReturn(3);

        when(shoulderRequest.getTargetReps())
                .thenReturn(8);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(exerciseRepository.findById(1L))
                .thenReturn(Optional.of(benchPress));

        when(exerciseRepository.findById(2L))
                .thenReturn(Optional.of(shoulderPress));

        WorkoutTemplate savedTemplate =
                mock(WorkoutTemplate.class);

        when(savedTemplate.getId())
                .thenReturn(3L);

        when(savedTemplate.getUser())
                .thenReturn(user);

        when(savedTemplate.getName())
                .thenReturn("Push Day");

        when(savedTemplate.getDescription())
                .thenReturn("Chest and shoulders");

        when(savedTemplate.getCreatedAt())
                .thenReturn(null);

        when(savedTemplate.getExercises())
                .thenReturn(List.of());

        when(workoutTemplateRepository.save(
                any(WorkoutTemplate.class)
        )).thenReturn(savedTemplate);

        WorkoutTemplateResponse response =
                workoutTemplateService.createTemplate(
                        request,
                        authentication
                );

        assertEquals(3L, response.getId());
        assertEquals("Push Day", response.getName());
        assertEquals(
                "Chest and shoulders",
                response.getDescription()
        );

        verify(workoutTemplateRepository)
                .save(any(WorkoutTemplate.class));
    }

    @Test
    void deleteTemplate_shouldRejectTemplateOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User templateOwner = mock(User.class);
        when(templateOwner.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(templateOwner);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(authenticatedUser));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutTemplateService.deleteTemplate(
                        3L,
                        authentication
                )
        );
    }

    @Test
    void deleteTemplate_shouldDeleteTemplateOwnedByAuthenticatedUser() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        when(template.getUser())
                .thenReturn(user);

        workoutTemplateService.deleteTemplate(
                3L,
                authentication
        );

        verify(workoutTemplateRepository)
                .delete(template);
    }

    @Test
    void getTemplateById_shouldThrowWhenTemplateDoesNotExist() {

        Authentication authentication = mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        User user = mock(User.class);

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutTemplateService.getTemplateById(
                        99L,
                        authentication
                )
        );
    }

    @Test
    void updateTemplate_shouldUpdateTemplateSuccessfully() {

        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        WorkoutTemplate template =
                mock(WorkoutTemplate.class);

        WorkoutTemplateRequest request =
                mock(WorkoutTemplateRequest.class);

        when(template.getUser())
                .thenReturn(user);

        when(template.getId())
                .thenReturn(3L);

        when(request.getName())
                .thenReturn("Upper Body");

        when(request.getDescription())
                .thenReturn("Chest, back and shoulders");

        when(request.getExercises())
                .thenReturn(List.of());

        when(template.getExercises())
                .thenReturn(new ArrayList<>());

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        when(workoutTemplateRepository.save(template))
                .thenReturn(template);

        when(template.getName())
                .thenReturn("Upper Body");

        when(template.getDescription())
                .thenReturn("Chest, back and shoulders");

        when(template.getCreatedAt())
                .thenReturn(null);

        when(template.getExercises())
                .thenReturn(new ArrayList<>());

        WorkoutTemplateResponse response =
                workoutTemplateService.updateTemplate(
                        3L,
                        request,
                        authentication
                );

        verify(template).setName("Upper Body");
        verify(template).setDescription(
                "Chest, back and shoulders"
        );

        verify(workoutTemplateRepository)
                .save(template);

        assertEquals(3L, response.getId());
        assertEquals("Upper Body", response.getName());
        assertEquals(
                "Chest, back and shoulders",
                response.getDescription()
        );
    }

    @Test
    void updateTemplate_shouldRejectTemplateOwnedByAnotherUser() {

        User authenticatedUser = mock(User.class);
        when(authenticatedUser.getId()).thenReturn(1L);

        User templateOwner = mock(User.class);
        when(templateOwner.getId()).thenReturn(2L);

        WorkoutTemplate template = mock(WorkoutTemplate.class);
        when(template.getId()).thenReturn(3L);
        when(template.getUser()).thenReturn(templateOwner);

        WorkoutTemplateRequest request =
                mock(WorkoutTemplateRequest.class);

        Authentication authentication =
                mock(Authentication.class);

        when(authentication.getName())
                .thenReturn("john");

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(authenticatedUser));

        when(workoutTemplateRepository.findById(3L))
                .thenReturn(Optional.of(template));

        assertThrows(
                ResourceNotFoundException.class,
                () -> workoutTemplateService.updateTemplate(
                        3L,
                        request,
                        authentication
                )
        );
    }
}