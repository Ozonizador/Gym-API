package com.gym.gym_api.controller;

import com.gym.gym_api.dto.ExerciseRequest;
import com.gym.gym_api.dto.ExerciseResponse;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExerciseResponse createExercise(
            @Valid @RequestBody ExerciseRequest request
    ) {
        return exerciseService.createExercise(request);
    }
}