package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.WorkoutExerciseRequest;
import com.gym.gym_api.dto.workout.WorkoutExerciseResponse;
import com.gym.gym_api.dto.workout.WorkoutRequest;
import com.gym.gym_api.dto.workout.WorkoutResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.Workout;
import com.gym.gym_api.entity.WorkoutExercise;
import com.gym.gym_api.entity.WorkoutTemplate;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutRepository;
import com.gym.gym_api.repository.WorkoutTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private static final Logger log =
            LoggerFactory.getLogger(WorkoutService.class);

    public WorkoutService(
            WorkoutRepository workoutRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository,
            WorkoutTemplateRepository workoutTemplateRepository
    ) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
        this.workoutTemplateRepository = workoutTemplateRepository;
    }

    public List<WorkoutResponse> getAllWorkouts(Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return workoutRepository.findAll()
                .stream()
                .filter(workout -> workout.getUser().getId().equals(user.getId()))
                .map(this::toResponse)
                .toList();
    }

    public WorkoutResponse getWorkoutById(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout not found: " + id
                        )
                );

        verifyOwnership(workout, user);

        return toResponse(workout);
    }

    public WorkoutResponse createWorkout(
            WorkoutRequest request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        WorkoutTemplate template = null;

        if (request.getWorkoutTemplateId() != null) {
            template = workoutTemplateRepository.findById(
                    request.getWorkoutTemplateId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Workout template not found: "
                                    + request.getWorkoutTemplateId()
                    )
            );

            verifyTemplateOwnership(template, user);
        }

        Workout workout = new Workout();

        workout.setUser(user);
        workout.setWorkoutTemplate(template);
        workout.setName(request.getName());
        workout.setWorkoutDate(request.getWorkoutDate());
        workout.setStartedAt(LocalDateTime.now());
        workout.setFinishedAt(null);
        workout.setFinished(false);
        workout.setDurationSeconds(null);
        workout.setCreatedAt(LocalDateTime.now());

        workout.setExercises(
                createWorkoutExercises(
                        workout,
                        request.getExercises()
                )
        );

        Workout savedWorkout = workoutRepository.save(workout);

        log.info(
                "Creating workout '{}' for user '{}'",
                request.getName(),
                authentication.getName()
        );

        return toResponse(savedWorkout);
    }

    public WorkoutResponse updateWorkout(
            Long id,
            WorkoutRequest request,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout not found: " + id
                        )
                );

        verifyOwnership(workout, user);

        workout.setName(request.getName());
        workout.setWorkoutDate(request.getWorkoutDate());

        workout.getExercises().clear();

        workout.getExercises().addAll(
                createWorkoutExercises(
                        workout,
                        request.getExercises()
                )
        );

        Workout updatedWorkout = workoutRepository.save(workout);

        return toResponse(updatedWorkout);
    }

    public void deleteWorkout(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout not found: " + id
                        )
                );

        verifyOwnership(workout, user);

        workoutRepository.delete(workout);
    }

    private User getAuthenticatedUser(Authentication authentication) {

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Authenticated user not found"
                        )
                );
    }

    private void verifyOwnership(Workout workout, User user) {

        if (!workout.getUser().getId().equals(user.getId())) {
            log.warn(
                    "User '{}' attempted to access workout {} owned by another user",
                    user.getUsername(),
                    workout.getId()
            );

            throw new ResourceNotFoundException(
                    "Workout not found: " + workout.getId()
            );
        }
    }

    private List<WorkoutExercise> createWorkoutExercises(
            Workout workout,
            List<WorkoutExerciseRequest> requests
    ) {

        List<WorkoutExercise> workoutExercises = new ArrayList<>();

        for (WorkoutExerciseRequest request : requests) {

            Exercise exercise = exerciseRepository.findById(
                    request.getExerciseId()
            ).orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Exercise not found: " + request.getExerciseId()
                    )
            );

            WorkoutExercise workoutExercise = new WorkoutExercise();

            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setSets(request.getSets());
            workoutExercise.setReps(request.getReps());
            workoutExercise.setWeight(request.getWeight());

            workoutExercises.add(workoutExercise);
        }

        return workoutExercises;
    }

    private void verifyTemplateOwnership(
            WorkoutTemplate template,
            User user
    ) {
        if (!template.getUser().getId().equals(user.getId())) {
            log.warn(
                    "User '{}' attempted to access workout template {} owned by another user",
                    user.getUsername(),
                    template.getId()
            );
            throw new ResourceNotFoundException(
                    "Workout template not found: " + template.getId()
            );
        }
    }

    private WorkoutResponse toResponse(Workout workout) {

        List<WorkoutExerciseResponse> exerciseResponses =
                workout.getExercises()
                        .stream()
                        .map(workoutExercise ->
                                new WorkoutExerciseResponse(
                                        workoutExercise.getExercise().getId(),
                                        workoutExercise.getExercise().getName(),
                                        workoutExercise.getSets(),
                                        workoutExercise.getReps(),
                                        workoutExercise.getWeight()
                                )
                        )
                        .toList();

        return new WorkoutResponse(
                workout.getId(),
                workout.getUser().getId(),
                workout.getWorkoutTemplate() != null
                        ? workout.getWorkoutTemplate().getId()
                        : null,
                workout.getWorkoutTemplate() != null
                        ? workout.getWorkoutTemplate().getName()
                        : null,
                workout.getName(),
                workout.getWorkoutDate(),
                workout.getDurationSeconds(),
                workout.getStartedAt(),
                workout.getFinishedAt(),
                workout.getCreatedAt(),
                workout.isFinished(),
                exerciseResponses
        );
    }

    public WorkoutResponse finishWorkout(
            Long id,
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Workout not found: " + id
                        )
                );

        verifyOwnership(workout, user);

        if (workout.isFinished()) {
            throw new IllegalArgumentException(
                    "Workout is already finished"
            );
        }

        LocalDateTime finishedAt = LocalDateTime.now();

        int durationSeconds = Math.toIntExact(Duration.between(
                workout.getStartedAt(),
                finishedAt
        ).toSeconds());

        if (durationSeconds <= 0) {
            durationSeconds = 1;
        }

        workout.setFinishedAt(finishedAt);
        workout.setFinished(true);
        workout.setDurationSeconds((int) durationSeconds);

        Workout finishedWorkout = workoutRepository.save(workout);

        log.info(
                "Finishing workout {} for user '{}'",
                id,
                authentication.getName()
        );

        return toResponse(finishedWorkout);
    }

    public Page<WorkoutResponse> getWorkoutHistory(
            Long templateId,
            Long exerciseId,
            LocalDate from,
            LocalDate to,
            int page,
            int size,
            Authentication authentication
    ) {
        User user = getAuthenticatedUser(authentication);

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Size must be between 1 and 100"
            );
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size
        );

        return workoutRepository.findWorkoutHistory(
                user.getId(),
                templateId,
                exerciseId,
                from,
                to,
                pageable
        ).map(this::toResponse);
    }
}