package com.gym.gym_api.service;

import com.gym.gym_api.dto.exercise.ExerciseMuscleGroupRequest;
import com.gym.gym_api.dto.exercise.ExerciseRequest;
import com.gym.gym_api.dto.exercise.ExerciseResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.ExerciseMuscleGroup;
import com.gym.gym_api.entity.MuscleGroup;
import com.gym.gym_api.entity.MuscleRole;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.MuscleGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private ExerciseRepository exerciseRepository;

    @Mock
    private MuscleGroupRepository muscleGroupRepository;

    private ExerciseService exerciseService;

    @BeforeEach
    void setUp() {
        exerciseService = new ExerciseService(
                exerciseRepository,
                muscleGroupRepository
        );
    }

    @Test
    void getExerciseById_shouldThrowWhenExerciseDoesNotExist() {

        when(exerciseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.getExerciseById(99L)
        );
    }

    @Test
    void createExercise_shouldRejectMissingMuscleGroup() {

        ExerciseRequest request = mock(ExerciseRequest.class);

        ExerciseMuscleGroupRequest muscleGroupRequest =
                mock(ExerciseMuscleGroupRequest.class);

        when(request.getName())
                .thenReturn("Bench Press");

        when(request.getDescription())
                .thenReturn("Barbell chest press");

        when(request.getMuscleGroups())
                .thenReturn(List.of(muscleGroupRequest));

        when(muscleGroupRequest.getMuscleGroupId())
                .thenReturn(999L);

        when(exerciseRepository.existsByName("Bench Press"))
                .thenReturn(false);

        when(muscleGroupRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.createExercise(request)
        );
    }

    @Test
    void createExercise_shouldRejectDuplicateName() {

        ExerciseRequest request = mock(ExerciseRequest.class);

        when(request.getName())
                .thenReturn("Bench Press");

        when(exerciseRepository.existsByName("Bench Press"))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> exerciseService.createExercise(request)
        );
    }

    @Test
    void createExercise_shouldCreateExerciseSuccessfully() {

        ExerciseRequest request = mock(ExerciseRequest.class);
        ExerciseMuscleGroupRequest muscleGroupRequest =
                mock(ExerciseMuscleGroupRequest.class);

        MuscleGroup muscleGroup = mock(MuscleGroup.class);
        Exercise savedExercise = mock(Exercise.class);

        when(request.getName())
                .thenReturn("Bench Press");

        when(request.getDescription())
                .thenReturn("Barbell chest press");

        when(request.getMuscleGroups())
                .thenReturn(List.of(muscleGroupRequest));

        when(muscleGroupRequest.getMuscleGroupId())
                .thenReturn(1L);

        when(muscleGroupRequest.getRole())
                .thenReturn(MuscleRole.PRIMARY);

        when(exerciseRepository.existsByName("Bench Press"))
                .thenReturn(false);

        when(muscleGroupRepository.findById(1L))
                .thenReturn(Optional.of(muscleGroup));

        when(savedExercise.getId())
                .thenReturn(10L);

        when(savedExercise.getName())
                .thenReturn("Bench Press");

        when(savedExercise.getDescription())
                .thenReturn("Barbell chest press");

        when(savedExercise.getMuscleGroups())
                .thenReturn(List.of());

        when(exerciseRepository.save(any(Exercise.class)))
                .thenReturn(savedExercise);

        ExerciseResponse response =
                exerciseService.createExercise(request);

        assertEquals(10L, response.getId());
        assertEquals("Bench Press", response.getName());
        assertEquals("Barbell chest press", response.getDescription());

        verify(exerciseRepository).save(any(Exercise.class));
    }

    @Test
    void updateExercise_shouldThrowWhenExerciseDoesNotExist() {

        ExerciseRequest request = mock(ExerciseRequest.class);

        when(exerciseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.updateExercise(99L, request)
        );
    }

    @Test
    void deleteExercise_shouldDeleteExistingExercise() {

        Exercise exercise = mock(Exercise.class);

        when(exerciseRepository.findById(10L))
                .thenReturn(Optional.of(exercise));

        exerciseService.deleteExercise(10L);

        verify(exerciseRepository).delete(exercise);
    }

    @Test
    void deleteExercise_shouldThrowWhenExerciseDoesNotExist() {

        when(exerciseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.deleteExercise(99L)
        );
    }

    @Test
    void updateExercise_shouldUpdateExerciseSuccessfully() {

        ExerciseRequest request = mock(ExerciseRequest.class);
        Exercise exercise = mock(Exercise.class);

        when(exerciseRepository.findById(10L))
                .thenReturn(Optional.of(exercise));

        when(request.getName())
                .thenReturn("Incline Bench Press");

        when(request.getDescription())
                .thenReturn("Incline barbell chest press");

        when(request.getMuscleGroups())
                .thenReturn(List.of());

        when(exercise.getId())
                .thenReturn(10L);

        when(exercise.getName())
                .thenReturn("Incline Bench Press");

        when(exercise.getDescription())
                .thenReturn("Incline barbell chest press");

        when(exercise.getMuscleGroups())
                .thenReturn(new ArrayList<>());

        when(exerciseRepository.save(exercise))
                .thenReturn(exercise);

        ExerciseResponse response =
                exerciseService.updateExercise(10L, request);

        verify(exercise).setName("Incline Bench Press");
        verify(exercise).setDescription(
                "Incline barbell chest press"
        );

        verify(exerciseRepository).save(exercise);

        assertEquals(10L, response.getId());
        assertEquals(
                "Incline Bench Press",
                response.getName()
        );
        assertEquals(
                "Incline barbell chest press",
                response.getDescription()
        );
    }

    @Test
    void updateExercise_shouldUpdateMuscleGroupsSuccessfully() {

        ExerciseRequest request = mock(ExerciseRequest.class);
        ExerciseMuscleGroupRequest muscleGroupRequest =
                mock(ExerciseMuscleGroupRequest.class);

        Exercise exercise = mock(Exercise.class);
        MuscleGroup muscleGroup = mock(MuscleGroup.class);

        List<ExerciseMuscleGroup> existingMuscleGroups =
                new ArrayList<>();

        when(exerciseRepository.findById(10L))
                .thenReturn(Optional.of(exercise));

        when(request.getName())
                .thenReturn("Incline Bench Press");

        when(request.getDescription())
                .thenReturn("Incline barbell chest press");

        when(request.getMuscleGroups())
                .thenReturn(List.of(muscleGroupRequest));

        when(muscleGroupRequest.getMuscleGroupId())
                .thenReturn(1L);

        when(muscleGroupRequest.getRole())
                .thenReturn(MuscleRole.PRIMARY);

        when(muscleGroupRepository.findById(1L))
                .thenReturn(Optional.of(muscleGroup));

        when(exercise.getId())
                .thenReturn(10L);

        when(exercise.getName())
                .thenReturn("Incline Bench Press");

        when(exercise.getDescription())
                .thenReturn("Incline barbell chest press");

        when(exercise.getMuscleGroups())
                .thenReturn(existingMuscleGroups);

        when(exerciseRepository.save(exercise))
                .thenReturn(exercise);

        ExerciseResponse response =
                exerciseService.updateExercise(10L, request);

        verify(exercise).setName("Incline Bench Press");
        verify(exercise).setDescription(
                "Incline barbell chest press"
        );

        verify(exerciseRepository).save(exercise);

        assertEquals(10L, response.getId());
        assertEquals(
                "Incline Bench Press",
                response.getName()
        );
    }

    @Test
    void createExercise_shouldCreateExerciseWithMuscleGroupRelationship() {

        ExerciseRequest request = mock(ExerciseRequest.class);
        ExerciseMuscleGroupRequest muscleGroupRequest =
                mock(ExerciseMuscleGroupRequest.class);

        MuscleGroup muscleGroup = mock(MuscleGroup.class);
        ExerciseMuscleGroup relationship =
                mock(ExerciseMuscleGroup.class);

        Exercise savedExercise = mock(Exercise.class);

        when(request.getName())
                .thenReturn("Bench Press");

        when(request.getDescription())
                .thenReturn("Barbell chest press");

        when(request.getMuscleGroups())
                .thenReturn(List.of(muscleGroupRequest));

        when(muscleGroupRequest.getMuscleGroupId())
                .thenReturn(1L);

        when(muscleGroupRequest.getRole())
                .thenReturn(MuscleRole.PRIMARY);

        when(exerciseRepository.existsByName("Bench Press"))
                .thenReturn(false);

        when(muscleGroupRepository.findById(1L))
                .thenReturn(Optional.of(muscleGroup));

        when(savedExercise.getId())
                .thenReturn(10L);

        when(savedExercise.getName())
                .thenReturn("Bench Press");

        when(savedExercise.getDescription())
                .thenReturn("Barbell chest press");

        when(savedExercise.getMuscleGroups())
                .thenReturn(List.of(relationship));

        when(relationship.getMuscleGroup())
                .thenReturn(muscleGroup);

        when(relationship.getRole())
                .thenReturn(MuscleRole.PRIMARY);

        when(muscleGroup.getId())
                .thenReturn(1L);

        when(exerciseRepository.save(any(Exercise.class)))
                .thenReturn(savedExercise);

        ExerciseResponse response =
                exerciseService.createExercise(request);

        assertEquals(1, response.getMuscleGroups().size());
        assertEquals(
                1L,
                response.getMuscleGroups()
                        .get(0)
                        .getMuscleGroupId()
        );
        assertEquals(
                MuscleRole.PRIMARY,
                response.getMuscleGroups()
                        .get(0)
                        .getRole()
        );
    }

    @Test
    void updateExercise_shouldRejectMissingMuscleGroup() {

        ExerciseRequest request = mock(ExerciseRequest.class);
        ExerciseMuscleGroupRequest muscleGroupRequest =
                mock(ExerciseMuscleGroupRequest.class);

        Exercise exercise = mock(Exercise.class);

        when(exerciseRepository.findById(10L))
                .thenReturn(Optional.of(exercise));

        when(request.getName())
                .thenReturn("Bench Press");

        when(request.getDescription())
                .thenReturn("Barbell chest press");

        when(request.getMuscleGroups())
                .thenReturn(List.of(muscleGroupRequest));

        when(muscleGroupRequest.getMuscleGroupId())
                .thenReturn(999L);

        when(exercise.getName())
                .thenReturn("Bench Press");

        when(exercise.getMuscleGroups())
                .thenReturn(new ArrayList<>());

        when(muscleGroupRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> exerciseService.updateExercise(10L, request)
        );
    }
}