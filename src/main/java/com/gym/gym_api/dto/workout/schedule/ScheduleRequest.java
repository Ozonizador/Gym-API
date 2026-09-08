package com.gym.gym_api.dto.workout.schedule;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class ScheduleRequest {

    @NotNull
    @FutureOrPresent
    private LocalDate scheduledDate;

    @NotNull
    private LocalTime scheduledTime;

    @NotNull
    private Long workoutTemplateId;

    @Size(max = 500)
    private String notes;

    public ScheduleRequest() {
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public Long getWorkoutTemplateId() {
        return workoutTemplateId;
    }

    public String getNotes() {
        return notes;
    }
}