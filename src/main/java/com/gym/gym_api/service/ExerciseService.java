package com.gym.gym_api.service;

import com.gym.gym_api.dto.ExerciseMuscleGroupRequest;
import com.gym.gym_api.dto.ExerciseMuscleGroupResponse;
import com.gym.gym_api.dto.ExerciseRequest;
import com.gym.gym_api.dto.ExerciseResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.ExerciseMuscleGroup;
import com.gym.gym_api.entity.MuscleGroup;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.MuscleGroupRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final MuscleGroupRepository muscleGroupRepository;

    public ExerciseService(
            ExerciseRepository exerciseRepository,
            MuscleGroupRepository muscleGroupRepository
    ) {
        this.exerciseRepository = exerciseRepository;
        this.muscleGroupRepository = muscleGroupRepository;
    }

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExerciseResponse getExerciseById(Long id) {

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exercise not found: " + id
                        )
                );

        return toResponse(exercise);
    }

    public ExerciseResponse createExercise(ExerciseRequest request) {

        if (exerciseRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Exercise name already exists"
            );
        }

        Exercise exercise = new Exercise();

        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setCreatedAt(LocalDateTime.now());

        exercise.setMuscleGroups(
                createMuscleGroupRelationships(
                        exercise,
                        request.getMuscleGroups()
                )
        );

        Exercise savedExercise = exerciseRepository.save(exercise);

        return toResponse(savedExercise);
    }

    public ExerciseResponse updateExercise(
            Long id,
            ExerciseRequest request
    ) {

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exercise not found: " + id
                        )
                );

        if (!exercise.getName().equals(request.getName())
                && exerciseRepository.existsByName(request.getName())) {

            throw new IllegalArgumentException(
                    "Exercise name already exists"
            );
        }

        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());

        exercise.getMuscleGroups().clear();

        exercise.getMuscleGroups().addAll(
                createMuscleGroupRelationships(
                        exercise,
                        request.getMuscleGroups()
                )
        );

        Exercise updatedExercise = exerciseRepository.save(exercise);

        return toResponse(updatedExercise);
    }

    public void deleteExercise(Long id) {

        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Exercise not found: " + id
                        )
                );

        exerciseRepository.delete(exercise);
    }

    private List<ExerciseMuscleGroup> createMuscleGroupRelationships(
            Exercise exercise,
            List<ExerciseMuscleGroupRequest> requests
    ) {

        List<ExerciseMuscleGroup> relationships = new ArrayList<>();

        if (requests == null) {
            return relationships;
        }

        for (ExerciseMuscleGroupRequest request : requests) {

            MuscleGroup muscleGroup = muscleGroupRepository
                    .findById(request.getMuscleGroupId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Muscle group not found: "
                                            + request.getMuscleGroupId()
                            )
                    );

            ExerciseMuscleGroup relationship =
                    new ExerciseMuscleGroup();

            relationship.setExercise(exercise);
            relationship.setMuscleGroup(muscleGroup);
            relationship.setRole(request.getRole());

            relationships.add(relationship);
        }

        return relationships;
    }

    private ExerciseResponse toResponse(Exercise exercise) {

        List<ExerciseMuscleGroupResponse> muscleGroupResponses =
                exercise.getMuscleGroups()
                        .stream()
                        .map(muscleGroup ->
                                new ExerciseMuscleGroupResponse(
                                        muscleGroup.getMuscleGroup().getId(),
                                        muscleGroup.getMuscleGroup().getName(),
                                        muscleGroup.getRole()
                                )
                        )
                        .toList();

        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getCreatedAt(),
                muscleGroupResponses
        );
    }
}