package com.gym.gym_api.dto.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class WorkoutRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private LocalDate workoutDate;

    @NotEmpty
    @Valid
    private List<WorkoutExerciseRequest> exercises;

    public WorkoutRequest() {
    }

    public String getName() {
        return name;
    }

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public List<WorkoutExerciseRequest> getExercises() {
        return exercises;
    }
}