package com.gym.gym_api.repository;

import com.gym.gym_api.entity.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutTemplateRepository
        extends JpaRepository<WorkoutTemplate, Long> {
}