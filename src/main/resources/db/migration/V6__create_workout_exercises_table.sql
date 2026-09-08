CREATE TABLE workout_exercises
(
    id          BIGSERIAL PRIMARY KEY,

    workout_id  BIGINT  NOT NULL,

    exercise_id BIGINT  NOT NULL,

    sets        INTEGER NOT NULL,

    reps        INTEGER NOT NULL,

    weight      DECIMAL(6, 2),

    CONSTRAINT fk_workout_exercise_workout
        FOREIGN KEY (workout_id)
            REFERENCES workouts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_workout_exercise_exercise
        FOREIGN KEY (exercise_id)
            REFERENCES exercises (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_workout_exercise
        UNIQUE (workout_id, exercise_id),

    CONSTRAINT chk_workout_exercise_sets
        CHECK (sets > 0),

    CONSTRAINT chk_workout_exercise_reps
        CHECK (reps > 0),

    CONSTRAINT chk_workout_exercise_weight
        CHECK (weight IS NULL OR weight >= 0)
);