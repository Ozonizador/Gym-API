package com.gym.gym_api.controller;

import com.gym.gym_api.dto.workout.template.WorkoutTemplateRequest;
import com.gym.gym_api.dto.workout.template.WorkoutTemplateResponse;
import com.gym.gym_api.service.WorkoutTemplateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workout-templates")
public class WorkoutTemplateController {

    private final WorkoutTemplateService workoutTemplateService;

    public WorkoutTemplateController(
            WorkoutTemplateService workoutTemplateService
    ) {
        this.workoutTemplateService = workoutTemplateService;
    }

    @GetMapping
    public List<WorkoutTemplateResponse> getAllTemplates(
            Authentication authentication
    ) {
        return workoutTemplateService.getAllTemplates(authentication);
    }

    @GetMapping("/{id}")
    public WorkoutTemplateResponse getTemplateById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return workoutTemplateService.getTemplateById(
                id,
                authentication
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutTemplateResponse createTemplate(
            @Valid @RequestBody WorkoutTemplateRequest request,
            Authentication authentication
    ) {
        return workoutTemplateService.createTemplate(
                request,
                authentication
        );
    }

    @PutMapping("/{id}")
    public WorkoutTemplateResponse updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody WorkoutTemplateRequest request,
            Authentication authentication
    ) {
        return workoutTemplateService.updateTemplate(
                id,
                request,
                authentication
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(
            @PathVariable Long id,
            Authentication authentication
    ) {
        workoutTemplateService.deleteTemplate(
                id,
                authentication
        );
    }
}