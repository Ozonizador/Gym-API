CREATE TABLE workout_templates
(
    id          BIGSERIAL PRIMARY KEY,

    user_id     BIGINT       NOT NULL,

    name        VARCHAR(100) NOT NULL,

    description VARCHAR(500),

    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_workout_template_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);