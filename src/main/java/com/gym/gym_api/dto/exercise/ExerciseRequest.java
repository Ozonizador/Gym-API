package com.gym.gym_api.dto.exercise;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ExerciseRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private List<ExerciseMuscleGroupRequest> muscleGroups;

    public ExerciseRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<ExerciseMuscleGroupRequest> getMuscleGroups() {
        return muscleGroups;
    }
}