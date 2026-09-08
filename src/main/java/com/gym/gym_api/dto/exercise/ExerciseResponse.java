package com.gym.gym_api.dto.exercise;

import java.time.LocalDateTime;
import java.util.List;

public class ExerciseResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<ExerciseMuscleGroupResponse> muscleGroups;

    public ExerciseResponse() {
    }

    public ExerciseResponse(
            Long id,
            String name,
            String description,
            LocalDateTime createdAt,
            List<ExerciseMuscleGroupResponse> muscleGroups
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.muscleGroups = muscleGroups;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<ExerciseMuscleGroupResponse> getMuscleGroups() {
        return muscleGroups;
    }
}