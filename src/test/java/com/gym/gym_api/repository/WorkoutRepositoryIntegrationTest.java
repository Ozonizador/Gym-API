package com.gym.gym_api.repository;

import com.gym.gym_api.entity.Exercise;
import com.gym.gym_api.entity.User;
import com.gym.gym_api.entity.Workout;
import com.gym.gym_api.entity.WorkoutExercise;
import com.gym.gym_api.entity.WorkoutTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkoutRepositoryIntegrationTest {

    @Autowired
    private WorkoutRepository workoutRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private WorkoutTemplateRepository workoutTemplateRepository;

    @Test
    void findWorkoutHistory_shouldReturnOnlyFinishedWorkoutsForUser() {

        User user = createUser(
                "history-user",
                "history-user@example.com"
        );

        User otherUser = createUser(
                "history-other-user",
                "history-other-user@example.com"
        );

        Workout finishedWorkout = createWorkout(
                user,
                null,
                "Finished Workout",
                LocalDate.of(2026, 9, 5),
                true
        );

        createWorkout(
                user,
                null,
                "Unfinished Workout",
                LocalDate.of(2026, 9, 6),
                false
        );

        createWorkout(
                otherUser,
                null,
                "Other User Workout",
                LocalDate.of(2026, 9, 7),
                true
        );

        Page<Workout> result =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                finishedWorkout.getId(),
                result.getContent().get(0).getId()
        );

        assertTrue(
                result.getContent().get(0).isFinished()
        );
    }

    @Test
    void findWorkoutHistory_shouldFilterByTemplate() {

        User user = createUser(
                "template-history-user",
                "template-history-user@example.com"
        );

        WorkoutTemplate template =
                createTemplate(
                        user,
                        "Push Template"
                );

        Workout matchingWorkout = createWorkout(
                user,
                template,
                "Push Workout",
                LocalDate.of(2026, 9, 5),
                true
        );

        createWorkout(
                user,
                null,
                "Workout Without Template",
                LocalDate.of(2026, 9, 6),
                true
        );

        Page<Workout> result =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        template.getId(),
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                matchingWorkout.getId(),
                result.getContent().get(0).getId()
        );
    }

    @Test
    void findWorkoutHistory_shouldFilterByExercise() {

        User user = createUser(
                "exercise-history-user",
                "exercise-history-user@example.com"
        );

        Exercise benchPress = createExercise(
                "History Bench Press"
        );

        Exercise squat = createExercise(
                "History Squat"
        );

        Workout matchingWorkout = createWorkout(
                user,
                null,
                "Bench Workout",
                LocalDate.of(2026, 9, 5),
                true
        );

        addExercise(
                matchingWorkout,
                benchPress
        );

        Workout otherWorkout = createWorkout(
                user,
                null,
                "Squat Workout",
                LocalDate.of(2026, 9, 6),
                true
        );

        addExercise(
                otherWorkout,
                squat
        );

        workoutRepository.save(matchingWorkout);
        workoutRepository.save(otherWorkout);

        Page<Workout> result =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        null,
                        benchPress.getId(),
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                matchingWorkout.getId(),
                result.getContent().get(0).getId()
        );
    }

    @Test
    void findWorkoutHistory_shouldFilterByDateRange() {

        User user = createUser(
                "date-history-user",
                "date-history-user@example.com"
        );

        Workout beforeRange = createWorkout(
                user,
                null,
                "Before Range",
                LocalDate.of(2026, 9, 1),
                true
        );

        Workout insideRange = createWorkout(
                user,
                null,
                "Inside Range",
                LocalDate.of(2026, 9, 5),
                true
        );

        Workout afterRange = createWorkout(
                user,
                null,
                "After Range",
                LocalDate.of(2026, 9, 10),
                true
        );

        Page<Workout> result =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        null,
                        null,
                        LocalDate.of(2026, 9, 5),
                        LocalDate.of(2026, 9, 10),
                        PageRequest.of(0, 20)
                );

        assertEquals(2, result.getTotalElements());

        assertEquals(
                afterRange.getId(),
                result.getContent().get(0).getId()
        );

        assertEquals(
                insideRange.getId(),
                result.getContent().get(1).getId()
        );

        assertTrue(
                result.getContent().stream()
                        .noneMatch(workout ->
                                workout.getId().equals(
                                        beforeRange.getId()
                                )
                        )
        );
    }

    @Test
    void findWorkoutHistory_shouldOrderByDateDescendingThenIdDescending() {

        User user = createUser(
                "order-history-user",
                "order-history-user@example.com"
        );

        Workout olderWorkout = createWorkout(
                user,
                null,
                "Older Workout",
                LocalDate.of(2026, 9, 1),
                true
        );

        Workout newerWorkout = createWorkout(
                user,
                null,
                "Newer Workout",
                LocalDate.of(2026, 9, 5),
                true
        );

        Page<Workout> result =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 20)
                );

        assertEquals(2, result.getTotalElements());

        assertEquals(
                newerWorkout.getId(),
                result.getContent().get(0).getId()
        );

        assertEquals(
                olderWorkout.getId(),
                result.getContent().get(1).getId()
        );
    }

    @Test
    void findWorkoutHistory_shouldPaginateResults() {

        User user = createUser(
                "pagination-history-user",
                "pagination-history-user@example.com"
        );

        createWorkout(
                user,
                null,
                "Workout 1",
                LocalDate.of(2026, 9, 1),
                true
        );

        createWorkout(
                user,
                null,
                "Workout 2",
                LocalDate.of(2026, 9, 2),
                true
        );

        createWorkout(
                user,
                null,
                "Workout 3",
                LocalDate.of(2026, 9, 3),
                true
        );

        Page<Workout> firstPage =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(0, 2)
                );

        Page<Workout> secondPage =
                workoutRepository.findWorkoutHistory(
                        user.getId(),
                        null,
                        null,
                        null,
                        null,
                        PageRequest.of(1, 2)
                );

        assertEquals(3, firstPage.getTotalElements());

        assertEquals(2, firstPage.getContent().size());

        assertEquals(1, secondPage.getContent().size());

        assertEquals(
                "Workout 3",
                firstPage.getContent()
                        .get(0)
                        .getName()
        );

        assertEquals(
                "Workout 2",
                firstPage.getContent()
                        .get(1)
                        .getName()
        );

        assertEquals(
                "Workout 1",
                secondPage.getContent()
                        .get(0)
                        .getName()
        );
    }

    private User createUser(
            String username,
            String email
    ) {
        User user = new User();

        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("password123");

        return userRepository.save(user);
    }

    private Exercise createExercise(String name) {

        Exercise exercise = new Exercise();

        exercise.setName(name);
        exercise.setDescription("History integration test");
        exercise.setCreatedAt(LocalDateTime.now());

        return exerciseRepository.save(exercise);
    }

    private WorkoutTemplate createTemplate(
            User user,
            String name
    ) {

        WorkoutTemplate template =
                new WorkoutTemplate();

        template.setUser(user);
        template.setName(name);
        template.setDescription("History integration test");
        template.setCreatedAt(LocalDateTime.now());
        template.setExercises(new ArrayList<>());

        return workoutTemplateRepository.save(template);
    }

    private Workout createWorkout(
            User user,
            WorkoutTemplate template,
            String name,
            LocalDate workoutDate,
            boolean finished
    ) {

        Workout workout = new Workout();

        workout.setUser(user);
        workout.setWorkoutTemplate(template);
        workout.setName(name);
        workout.setWorkoutDate(workoutDate);
        workout.setCreatedAt(LocalDateTime.now());
        workout.setStartedAt(
                LocalDateTime.of(
                        2026,
                        9,
                        workoutDate.getDayOfMonth(),
                        10,
                        0
                )
        );
        workout.setFinished(finished);

        if (finished) {
            workout.setFinishedAt(
                    LocalDateTime.of(
                            2026,
                            9,
                            workoutDate.getDayOfMonth(),
                            11,
                            0
                    )
            );
            workout.setDurationSeconds(3600);
        }

        workout.setExercises(new ArrayList<>());

        return workoutRepository.save(workout);
    }

    private void addExercise(
            Workout workout,
            Exercise exercise
    ) {

        WorkoutExercise workoutExercise =
                new WorkoutExercise();

        workoutExercise.setWorkout(workout);
        workoutExercise.setExercise(exercise);
        workoutExercise.setSets(4);
        workoutExercise.setReps(10);
        workoutExercise.setWeight(
                BigDecimal.valueOf(80)
        );

        workout.getExercises().add(workoutExercise);
    }
}