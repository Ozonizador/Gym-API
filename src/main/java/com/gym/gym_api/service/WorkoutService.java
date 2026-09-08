package com.gym.gym_api.service;

import com.gym.gym_api.dto.workout.WorkoutExerciseRequest;
import com.gym.gym_api.dto.workout.WorkoutExerciseResponse;
import com.gym.gym_api.dto.workout.WorkoutRequest;
import com.gym.gym_api.dto.workout.WorkoutResponse;
import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.Workout;
import com.gym.gym_api.entity.WorkoutExercise;
import com.gym.gym_api.exception.ResourceNotFoundException;
import com.gym.gym_api.repository.ExerciseRepository;
import com.gym.gym_api.repository.UserRepository;
import com.gym.gym_api.repository.WorkoutRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    public WorkoutService(
            WorkoutRepository workoutRepository,
            UserRepository userRepository,
            ExerciseRepository exerciseRepository
    ) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
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

        Workout workout = new Workout();

        workout.setUser(user);
        workout.setName(request.getName());
        workout.setWorkoutDate(request.getWorkoutDate());
        workout.setDurationMinutes(request.getDurationMinutes());
        workout.setCreatedAt(LocalDateTime.now());

        workout.setExercises(
                createWorkoutExercises(
                        workout,
                        request.getExercises()
                )
        );

        Workout savedWorkout = workoutRepository.save(workout);

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
        workout.setDurationMinutes(request.getDurationMinutes());

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
                workout.getName(),
                workout.getWorkoutDate(),
                workout.getDurationMinutes(),
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

        workout.setFinished(true);

        Workout finishedWorkout = workoutRepository.save(workout);

        return toResponse(finishedWorkout);
    }
}