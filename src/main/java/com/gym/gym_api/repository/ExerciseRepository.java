package com.gym.gym_api.repository;

import com.gym.gym_api.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    boolean existsByName(String name);
}