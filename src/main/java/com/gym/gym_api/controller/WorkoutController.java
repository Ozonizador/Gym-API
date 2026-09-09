package com.gym.gym_api.controller;

import com.gym.gym_api.dto.workout.WorkoutRequest;
import com.gym.gym_api.dto.workout.WorkoutResponse;
import com.gym.gym_api.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public List<WorkoutResponse> getAllWorkouts(
            Authentication authentication
    ) {
        return workoutService.getAllWorkouts(authentication);
    }

    @GetMapping("/history")
    public Page<WorkoutResponse> getWorkoutHistory(
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long exerciseId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        return workoutService.getWorkoutHistory(
                templateId,
                exerciseId,
                from,
                to,
                page,
                size,
                authentication
        );
    }

    @GetMapping("/{id}")
    public WorkoutResponse getWorkoutById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return workoutService.getWorkoutById(id, authentication);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse createWorkout(
            @Valid @RequestBody WorkoutRequest request,
            Authentication authentication
    ) {
        return workoutService.createWorkout(request, authentication);
    }

    @PutMapping("/{id}")
    public WorkoutResponse updateWorkout(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutRequest request,
            Authentication authentication
    ) {
        return workoutService.updateWorkout(
                id,
                request,
                authentication
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkout(
            @PathVariable Long id,
            Authentication authentication
    ) {
        workoutService.deleteWorkout(id, authentication);
    }

    @PatchMapping("/{id}/finish")
    public WorkoutResponse finishWorkout(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return workoutService.finishWorkout(id, authentication);
    }
}