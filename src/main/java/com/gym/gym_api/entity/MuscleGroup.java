package com.gym.gym_api.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "muscle_groups")
public class MuscleGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "muscleGroup")
    private List<ExerciseMuscleGroup> exercises = new ArrayList<>();

    public MuscleGroup() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ExerciseMuscleGroup> getExercises() {
        return exercises;
    }

    public void setExercises(List<ExerciseMuscleGroup> exercises) {
        this.exercises = exercises;
    }
}