package com.gym.gym_api.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "exercise_muscle_groups",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"exercise_id", "muscle_group_id"}
                )
        }
)
public class ExerciseMuscleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "muscle_group_id", nullable = false)
    private MuscleGroup muscleGroup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MuscleRole role;

    public ExerciseMuscleGroup() {
    }

    public Long getId() {
        return id;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public MuscleGroup getMuscleGroup() {
        return muscleGroup;
    }

    public MuscleRole getRole() {
        return role;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public void setMuscleGroup(MuscleGroup muscleGroup) {
        this.muscleGroup = muscleGroup;
    }

    public void setRole(MuscleRole role) {
        this.role = role;
    }
}