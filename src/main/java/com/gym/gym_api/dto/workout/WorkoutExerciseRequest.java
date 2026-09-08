package com.gym.gym_api.dto.workout;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WorkoutExerciseRequest {

    @NotNull
    private Long exerciseId;

    @NotNull
    @Min(1)
    private Integer sets;

    @NotNull
    @Min(1)
    private Integer reps;

    @DecimalMin("0.0")
    private BigDecimal weight;

    public WorkoutExerciseRequest() {
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public Integer getSets() {
        return sets;
    }

    public Integer getReps() {
        return reps;
    }

    public BigDecimal getWeight() {
        return weight;
    }
}