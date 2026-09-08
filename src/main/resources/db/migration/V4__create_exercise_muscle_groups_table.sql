CREATE TABLE exercise_muscle_groups (
                                        id BIGSERIAL PRIMARY KEY,

                                        exercise_id BIGINT NOT NULL,
                                        muscle_group_id BIGINT NOT NULL,

                                        role VARCHAR(20) NOT NULL,

                                        CONSTRAINT fk_exercise_muscle_group_exercise
                                            FOREIGN KEY (exercise_id)
                                                REFERENCES exercises(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_exercise_muscle_group_muscle
                                            FOREIGN KEY (muscle_group_id)
                                                REFERENCES muscle_groups(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT uq_exercise_muscle_group
                                            UNIQUE (exercise_id, muscle_group_id),

                                        CONSTRAINT chk_exercise_muscle_group_role
                                            CHECK (role IN ('PRIMARY', 'SECONDARY'))
);