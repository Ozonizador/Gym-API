CREATE TABLE workouts
(
    id               BIGSERIAL PRIMARY KEY,

    user_id          BIGINT       NOT NULL,

    name             VARCHAR(100) NOT NULL,

    workout_date     DATE         NOT NULL,

    duration_minutes INTEGER,

    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_workout_duration
        CHECK (duration_minutes IS NULL OR duration_minutes > 0)
);