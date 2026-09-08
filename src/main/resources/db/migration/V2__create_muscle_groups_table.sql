CREATE TABLE muscle_groups
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO muscle_groups (name)
VALUES ('Chest'),
       ('Back'),
       ('Shoulders'),
       ('Biceps'),
       ('Triceps'),
       ('Forearms'),
       ('Abdominals'),
       ('Quadriceps'),
       ('Hamstrings'),
       ('Glutes'),
       ('Calves');