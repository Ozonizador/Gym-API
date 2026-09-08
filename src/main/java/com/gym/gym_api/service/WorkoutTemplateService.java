package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.WorkoutTemplateExerciseRequest;
import com.gym.gym_api.dto.workout.WorkoutTemplateExerciseResponse;
import com.gym.gym_api.dto.workout.WorkoutTemplateRequest;
import com.gym.gym_api.dto.workout.WorkoutTemplateResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.entity.WorkoutTemplateExercise;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutTemplateService {

    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutTemplateService(
            WorkoutTemplateRepository workoutTemplateRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.workoutTemplateRepository = workoutTemplateRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public List<WorkoutTemplateResponse> getAllTemplates(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return workoutTemplateRepository.findAll()
                .stream()
                .filter(template ->
                        template.getUser().getId().equals(user.getId())
                )
                .map(this::toResponse)
                .toList();
    }

    public WorkoutTemplateResponse getTemplateById(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        WorkoutTemplate template = workoutTemplateRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout template not found: " + id
                        )
                );

        verifyOwnership(template, user);

        return toResponse(template);
    }

    public WorkoutTemplateResponse createTemplate(
            WorkoutTemplateRequest request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        WorkoutTemplate template = new WorkoutTemplate();

        template.setUser(user);
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCreatedAt(LocalDateTime.now());

        template.setExercises(
                createTemplateExercises(
                        template,
                        request.getExercises()
                )
        );

        WorkoutTemplate savedTemplate =
                workoutTemplateRepository.save(template);

        return toResponse(savedTemplate);
    }

    public WorkoutTemplateResponse updateTemplate(
            Long id,
            WorkoutTemplateRequest request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        WorkoutTemplate template =
                workoutTemplateRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workout template not found: " + id
                                )
                        );

        verifyOwnership(template, user);

        template.setName(request.getName());
        template.setDescription(request.getDescription());

        template.getExercises().clear();

        template.getExercises().addAll(
                createTemplateExercises(
                        template,
                        request.getExercises()
                )
        );

        WorkoutTemplate updatedTemplate =
                workoutTemplateRepository.save(template);

        return toResponse(updatedTemplate);
    }

    public void deleteTemplate(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        WorkoutTemplate template =
                workoutTemplateRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Workout template not found: " + id
                                )
                        );

        verifyOwnership(template, user);

        workoutTemplateRepository.delete(template);
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void verifyOwnership(
            WorkoutTemplate template,
            User user
    ) {

        if (!template.getUser().getId().equals(user.getId())) {

            throw new ResourceNotFoundException(
                    "Workout template not found: " + template.getId()
            );
        }
    }

    private List<WorkoutTemplateExercise> createTemplateExercises(
            WorkoutTemplate template,
            List<WorkoutTemplateExerciseRequest> requests
    ) {

        List<WorkoutTemplateExercise> exercises =
                new ArrayList<>();

        for (WorkoutTemplateExerciseRequest request : requests) {

            Exercise exercise =
                    exerciseRepository.findById(
                            request.getExerciseId()
                    ).orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Exercise not found: "
                                            + request.getExerciseId()
                            )
                    );

            WorkoutTemplateExercise templateExercise =
                    new WorkoutTemplateExercise();

            templateExercise.setWorkoutTemplate(template);
            templateExercise.setExercise(exercise);
            templateExercise.setPosition(request.getPosition());
            templateExercise.setTargetSets(request.getTargetSets());
            templateExercise.setTargetReps(request.getTargetReps());

            exercises.add(templateExercise);
        }

        return exercises;
    }

    private WorkoutTemplateResponse toResponse(
            WorkoutTemplate template
    ) {

        List<WorkoutTemplateExerciseResponse> exerciseResponses =
                template.getExercises()
                        .stream()
                        .sorted((a, b) ->
                                a.getPosition()
                                        .compareTo(b.getPosition())
                        )
                        .map(templateExercise ->
                                new WorkoutTemplateExerciseResponse(
                                        templateExercise.getExercise().getId(),
                                        templateExercise.getExercise().getName(),
                                        templateExercise.getPosition(),
                                        templateExercise.getTargetSets(),
                                        templateExercise.getTargetReps()
                                )
                        )
                        .toList();

        return new WorkoutTemplateResponse(
                template.getId(),
                template.getUser().getId(),
                template.getName(),
                template.getDescription(),
                template.getCreatedAt(),
                exerciseResponses
        );
    }
}