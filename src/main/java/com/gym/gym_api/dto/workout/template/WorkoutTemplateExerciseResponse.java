package com.gym.gym_api.dto.workout.template;

public class WorkoutTemplateExerciseResponse {

    private Long exerciseId;
    private String exerciseName;
    private Integer position;
    private Integer targetSets;
    private Integer targetReps;

    public WorkoutTemplateExerciseResponse() {
    }

    public WorkoutTemplateExerciseResponse(
            Long exerciseId,
            String exerciseName,
            Integer position,
            Integer targetSets,
            Integer targetReps
    ) {
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.position = position;
        this.targetSets = targetSets;
        this.targetReps = targetReps;
    }

    public Long getExerciseId() {
        return exerciseId;
    }

    public String getExerciseName() {
        return exerciseName;
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