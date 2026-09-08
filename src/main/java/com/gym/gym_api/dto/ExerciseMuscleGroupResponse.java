package com.gym.gym_api.dto;

import com.gym.gym_api.entity.MuscleRole;

public class ExerciseMuscleGroupResponse {

    private Long muscleGroupId;
    private String muscleGroupName;
    private MuscleRole role;

    public ExerciseMuscleGroupResponse() {
    }

    public ExerciseMuscleGroupResponse(
            Long muscleGroupId,
            String muscleGroupName,
            MuscleRole role
    ) {
        this.muscleGroupId = muscleGroupId;
        this.muscleGroupName = muscleGroupName;
        this.role = role;
    }

    public Long getMuscleGroupId() {
        return muscleGroupId;
    }

    public String getMuscleGroupName() {
        return muscleGroupName;
    }

    public MuscleRole getRole() {
        return role;
    }
}