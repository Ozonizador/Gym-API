package com.gym.gym_api.repository;

import com.gym.gym_api.entity.Workout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {

    @Query("""
            SELECT DISTINCT w
            FROM Workout w
            LEFT JOIN w.exercises we
            WHERE w.user.id = :userId
              AND w.finished = true
              AND (:templateId IS NULL OR w.workoutTemplate.id = :templateId)
              AND (:exerciseId IS NULL OR we.exercise.id = :exerciseId)
              AND w.workoutDate >= COALESCE(:from, w.workoutDate)
              AND w.workoutDate <= COALESCE(:to, w.workoutDate)
            ORDER BY w.workoutDate DESC, w.id DESC
            """)
    Page<Workout> findWorkoutHistory(
            @Param("userId") Long userId,
            @Param("templateId") Long templateId,
            @Param("exerciseId") Long exerciseId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );
}