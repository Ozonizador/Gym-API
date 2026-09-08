package com.gym.gym_api.controller;

import com.gym.gym_api.dto.exercise.ExerciseRequest;
import com.gym.gym_api.dto.exercise.ExerciseResponse;
import com.gym.gym_api.service.ExerciseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<ExerciseResponse> getAllExercises() {
        return exerciseService.getAllExercises();
    }

    @GetMapping("/{id}")
    public ExerciseResponse getExerciseById(
            @PathVariable Long id
    ) {
        return exerciseService.getExerciseById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(
            @Valid @RequestBody ExerciseRequest request
    ) {
        return exerciseService.createExercise(request);
    }

    @PutMapping("/{id}")
    public ExerciseResponse updateExercise(
            @PathVariable Long id,
            @Valid @RequestBody ExerciseRequest request
    ) {
        return exerciseService.updateExercise(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
    }
}