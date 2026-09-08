ALTER TABLE workouts
    RENAME COLUMN duration_minutes TO duration_seconds;

ALTER TABLE workouts
    ADD COLUMN started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE workouts
    ADD COLUMN finished_at TIMESTAMP;