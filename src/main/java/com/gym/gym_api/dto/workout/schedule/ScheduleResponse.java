package com.gym.gym_api.dto.workout.schedule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ScheduleResponse {

    private Long id;
    private Long userId;
    private Long workoutTemplateId;
    private String workoutTemplateName;
    private LocalDate scheduledDate;
    private LocalTime scheduledTime;
    private String notes;
    private LocalDateTime createdAt;

    public ScheduleResponse() {
    }

    public ScheduleResponse(
            Long id,
            Long userId,
            Long workoutTemplateId,
            String workoutTemplateName,
            LocalDate scheduledDate,
            LocalTime scheduledTime,
            String notes,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.workoutTemplateId = workoutTemplateId;
        this.workoutTemplateName = workoutTemplateName;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getWorkoutTemplateId() {
        return workoutTemplateId;
    }

    public String getWorkoutTemplateName() {
        return workoutTemplateName;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}