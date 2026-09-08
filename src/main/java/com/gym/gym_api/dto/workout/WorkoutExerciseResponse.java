package com.gym.gym_api.dto.workout;

import java.math.BigDecimal;

public class WorkoutExerciseResponse {

    private Long exerciseId;
    private String exerciseName;
    private Integer sets;
    private Integer reps;
    private BigDecimal weight;

    public WorkoutExerciseResponse() {
    }

    public WorkoutExerciseResponse(
            Long exerciseId,
            String exerciseName,
            Integer sets,
            Integer reps,
            BigDecimal weight
    ) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.sets = sets;
        this.reps = reps;
        this.weight = weight;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
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