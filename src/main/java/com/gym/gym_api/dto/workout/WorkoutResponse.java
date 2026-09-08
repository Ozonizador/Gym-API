package com.gym.gym_api.dto.workout;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class WorkoutResponse {

    private Long id;
    private Long userId;
    private String name;
    private LocalDate workoutDate;
    private Integer durationSeconds;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private boolean finished;
    private List<WorkoutExerciseResponse> exercises;

    public WorkoutResponse() {
    }

    public WorkoutResponse(
            Long id,
            Long userId,
            String name,
            LocalDate workoutDate,
            Integer durationSeconds,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime createdAt,
            boolean finished,
            List<WorkoutExerciseResponse> exercises
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.workoutDate = workoutDate;
        this.durationSeconds = durationSeconds;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.createdAt = createdAt;
        this.finished = finished;
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

    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<WorkoutExerciseResponse> getExercises() {
        return exercises;
    }
}