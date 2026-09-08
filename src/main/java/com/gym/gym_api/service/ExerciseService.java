package com.gym.gym_api.service;

import com.gym.gym_api.dto.ExerciseMuscleGroupRequest;
import com.gym.gym_api.dto.ExerciseMuscleGroupResponse;
import com.gym.gym_api.dto.ExerciseRequest;
import com.gym.gym_api.dto.ExerciseResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.ExerciseMuscleGroup;
import com.gym.gym_api.entity.MuscleGroup;
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

    public ExerciseResponse createExercise(ExerciseRequest request) {

        if (exerciseRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Exercise name already exists");
        }

        Exercise exercise = new Exercise();

        exercise.setName(request.getName());
        exercise.setDescription(request.getDescription());
        exercise.setCreatedAt(LocalDateTime.now());

        List<ExerciseMuscleGroup> muscleGroups = new ArrayList<>();

        if (request.getMuscleGroups() != null) {

            for (ExerciseMuscleGroupRequest muscleGroupRequest : request.getMuscleGroups()) {

                MuscleGroup muscleGroup = muscleGroupRepository
                        .findById(muscleGroupRequest.getMuscleGroupId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Muscle group not found: "
                                                + muscleGroupRequest.getMuscleGroupId()
                                )
                        );

                ExerciseMuscleGroup exerciseMuscleGroup = new ExerciseMuscleGroup();

                exerciseMuscleGroup.setExercise(exercise);
                exerciseMuscleGroup.setMuscleGroup(muscleGroup);
                exerciseMuscleGroup.setRole(muscleGroupRequest.getRole());

                muscleGroups.add(exerciseMuscleGroup);
            }
        }

        exercise.setMuscleGroups(muscleGroups);

        Exercise savedExercise = exerciseRepository.save(exercise);

        return toResponse(savedExercise);
    }

    private ExerciseResponse toResponse(Exercise exercise) {

        List<ExerciseMuscleGroupResponse> muscleGroupResponses =
                exercise.getMuscleGroups()
                        .stream()
                        .map(muscleGroup -> new ExerciseMuscleGroupResponse(
                                muscleGroup.getMuscleGroup().getId(),
                                muscleGroup.getMuscleGroup().getName(),
                                muscleGroup.getRole()
                        ))
                        .toList();

        return new ExerciseResponse(
                exercise.getId(),
                exercise.getName(),
                exercise.getDescription(),
                exercise.getCreatedAt(),
                muscleGroupResponses
        );
    }

    public List<ExerciseResponse> getAllExercises() {
        return exerciseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }
}