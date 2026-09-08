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

    public List<WorkoutResponse> getAllWorkouts() {
        return workoutRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkoutResponse getWorkoutById(Long id) {
        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found: " + id)
                );

        return toResponse(workout);
    }

    public WorkoutResponse createWorkout(WorkoutRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + request.getUserId()
                        )
                );

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

    public WorkoutResponse updateWorkout(Long id, WorkoutRequest request) {

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found: " + id)
                );

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + request.getUserId()
                        )
                );

        workout.setUser(user);
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

    public void deleteWorkout(Long id) {

        Workout workout = workoutRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workout not found: " + id)
                );

        workoutRepository.delete(workout);
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
                exerciseResponses
        );
    }
}