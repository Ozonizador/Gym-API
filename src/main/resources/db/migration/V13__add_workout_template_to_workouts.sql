ALTER TABLE workouts
    ADD COLUMN workout_template_id BIGINT;

ALTER TABLE workouts
    ADD CONSTRAINT fk_workout_template
        FOREIGN KEY (workout_template_id)
            REFERENCES workout_templates(id)
            ON DELETE SET NULL;