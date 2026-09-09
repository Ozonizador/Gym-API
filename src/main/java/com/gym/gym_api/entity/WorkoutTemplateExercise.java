package com.gym.gym_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "workout_template_exercises",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"workout_template_id", "exercise_id"}
                )
        }
)
public class WorkoutTemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workout_template_id", nullable = false)
    private WorkoutTemplate workoutTemplate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "target_sets", nullable = false)
    private Integer targetSets;

    @Column(name = "target_reps", nullable = false)
    private Integer targetReps;

    public WorkoutTemplateExercise() {
    }

    public Long getId() {
        return id;
    }

    public WorkoutTemplate getWorkoutTemplate() {
        return workoutTemplate;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public Integer getPosition() {
        return position;
    }

    public Integer getTargetSets() {
        return targetSets;
    }

    public Integer getTargetReps() {
        return targetReps;
    }

    public void setWorkoutTemplate(WorkoutTemplate workoutTemplate) {
        this.workoutTemplate = workoutTemplate;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setTargetSets(Integer targetSets) {
        this.targetSets = targetSets;
    }

    public void setTargetReps(Integer targetReps) {
        this.targetReps = targetReps;
    }
}