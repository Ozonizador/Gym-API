package com.gym.gym_api.dto.exercise;

import com.gym.gym_api.entity.MuscleRole;
import jakarta.validation.constraints.NotNull;

public class ExerciseMuscleGroupRequest {

    @NotNull
    private Long muscleGroupId;

    @NotNull
    private MuscleRole role;

    public ExerciseMuscleGroupRequest() {
    }

    public Long getMuscleGroupId() {
        return muscleGroupId;
    }

    public MuscleRole getRole() {
        return role;
    }
}