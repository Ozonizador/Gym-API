CREATE TABLE workout_template_exercises
(
    id                  BIGSERIAL PRIMARY KEY,

    workout_template_id BIGINT  NOT NULL,

    exercise_id         BIGINT  NOT NULL,

    position            INTEGER NOT NULL,

    target_sets         INTEGER NOT NULL,

    target_reps         INTEGER NOT NULL,

    CONSTRAINT fk_template_exercise_template
        FOREIGN KEY (workout_template_id)
            REFERENCES workout_templates (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_template_exercise_exercise
        FOREIGN KEY (exercise_id)
            REFERENCES exercises (id)
            ON DELETE RESTRICT,

    CONSTRAINT uq_template_exercise
        UNIQUE (workout_template_id, exercise_id),

    CONSTRAINT chk_template_exercise_position
        CHECK (position > 0),

    CONSTRAINT chk_template_exercise_sets
        CHECK (target_sets > 0),

    CONSTRAINT chk_template_exercise_reps
        CHECK (target_reps > 0)
);