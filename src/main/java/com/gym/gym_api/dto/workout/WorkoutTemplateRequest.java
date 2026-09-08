package com.gym.gym_api.dto.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class WorkoutTemplateRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotEmpty
    @Valid
    private List<WorkoutTemplateExerciseRequest> exercises;

    public WorkoutTemplateRequest() {
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<WorkoutTemplateExerciseRequest> getExercises() {
        return exercises;
    }
}