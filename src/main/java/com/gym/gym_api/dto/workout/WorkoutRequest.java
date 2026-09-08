package com.gym.gym_api.dto.workout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

public class WorkoutRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private LocalDate workoutDate;

    @NotEmpty
    @Valid
    private List<WorkoutExerciseRequest> exercises;

    @NotNull
    @Min(1)
    private Integer durationMinutes;

    public WorkoutRequest() {
    }

    public Long getUserId() {
        return userId;
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

    public Integer getDurationMinutes() {
        return durationMinutes;
    }
}