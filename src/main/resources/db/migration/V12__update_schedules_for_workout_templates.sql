ALTER TABLE schedules
DROP
CONSTRAINT fk_schedule_workout;

ALTER TABLE schedules
    RENAME COLUMN workout_id TO workout_template_id;

ALTER TABLE schedules
    ADD CONSTRAINT fk_schedule_workout_template
        FOREIGN KEY (workout_template_id)
            REFERENCES workout_templates (id)
            ON DELETE CASCADE;