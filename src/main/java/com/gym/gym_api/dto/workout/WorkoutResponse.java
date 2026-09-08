package com.gym.gym_api.dto.workout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WorkoutResponse {

    private Long id;
    private Long userId;
    private String name;
    private LocalDate workoutDate;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
    private List<WorkoutExerciseResponse> exercises;

    public WorkoutResponse() {
    }

    public WorkoutResponse(
            Long id,
            Long userId,
            String name,
            LocalDate workoutDate,
            Integer durationMinutes,
            LocalDateTime createdAt,
            List<WorkoutExerciseResponse> exercises
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.workoutDate = workoutDate;
        this.durationMinutes = durationMinutes;
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

    public LocalDate getWorkoutDate() {
        return workoutDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<WorkoutExerciseResponse> getExercises() {
        return exercises;
    }
}