package com.gym.gym_api.repository;

import com.gym.gym_api.entity.WorkoutTemplateExercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTemplateExerciseRepository
        extends JpaRepository<WorkoutTemplateExercise, Long> {
}