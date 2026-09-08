package com.gym.gym_api.dto.workout;

import java.time.LocalDateTime;
import java.util.List;

public class WorkoutTemplateResponse {

    private Long id;
    private Long userId;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private List<WorkoutTemplateExerciseResponse> exercises;

    public WorkoutTemplateResponse() {
    }

    public WorkoutTemplateResponse(
            Long id,
            Long userId,
            String name,
            String description,
            LocalDateTime createdAt,
            List<WorkoutTemplateExerciseResponse> exercises
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.exercises = exercises;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
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

    public List<WorkoutTemplateExerciseResponse> getExercises() {
        return exercises;
    }
}