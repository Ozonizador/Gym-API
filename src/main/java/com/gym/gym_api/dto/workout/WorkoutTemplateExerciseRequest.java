package com.gym.gym_api.dto.workout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class WorkoutTemplateExerciseRequest {

    @NotNull
    private Long exerciseId;

    @NotNull
    @Min(1)
    private Integer position;

    @NotNull
    @Min(1)
    private Integer targetSets;

    @NotNull
    @Min(1)
    private Integer targetReps;

    public WorkoutTemplateExerciseRequest() {
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getTargetSets() {
        return targetSets;
    }

    public Integer getTargetReps() {
        return targetReps;
    }
}