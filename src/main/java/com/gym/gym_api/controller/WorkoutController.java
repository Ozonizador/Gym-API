package com.gym.gym_api.controller;

import com.gym.gym_api.dto.workout.WorkoutRequest;
import com.gym.gym_api.dto.workout.WorkoutResponse;
import com.gym.gym_api.service.WorkoutService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @GetMapping
    public List<WorkoutResponse> getAllWorkouts() {
        return workoutService.getAllWorkouts();
    }

    @GetMapping("/{id}")
    public WorkoutResponse getWorkoutById(@PathVariable Long id) {
        return workoutService.getWorkoutById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutResponse createWorkout(
            @Valid @RequestBody WorkoutRequest request
    ) {
        return workoutService.createWorkout(request);
    }

    @PutMapping("/{id}")
    public WorkoutResponse updateWorkout(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutRequest request
    ) {
        return workoutService.updateWorkout(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkout(@PathVariable Long id) {
        workoutService.deleteWorkout(id);
    }
}