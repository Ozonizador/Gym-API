CREATE TABLE schedules
(
    id             BIGSERIAL PRIMARY KEY,

    user_id        BIGINT    NOT NULL,

    workout_id     BIGINT    NOT NULL,

    scheduled_date DATE      NOT NULL,

    scheduled_time TIME      NOT NULL,

    notes          VARCHAR(500),

    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_schedule_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_schedule_workout
        FOREIGN KEY (workout_id)
            REFERENCES workouts (id)
            ON DELETE CASCADE
);