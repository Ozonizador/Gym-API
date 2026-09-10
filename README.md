# Gym API

A RESTful backend API for managing exercises, workout templates, and workout sessions, with JWT authentication and
PostgreSQL persistence.

Built as a backend-focused project using **Java 21** and **Spring Boot**, with an emphasis on clean architecture, secure
resource ownership, relational data modelling, and API documentation.

## Features

* JWT-based authentication with Spring Security
* BCrypt password hashing
* User management and resource ownership
* Exercise CRUD operations
* Exercise-to-muscle-group relationships
* Reusable workout templates
* Workout CRUD operations
* Automatic workout duration tracking
* Workout completion tracking
* PostgreSQL database with Flyway migrations
* Bean Validation and centralized exception handling
* OpenAPI / Swagger documentation
* Dockerized PostgreSQL development environment

## Tech Stack

| Technology        | Purpose                        |
|-------------------|--------------------------------|
| Java 21           | Programming language           |
| Spring Boot       | Backend framework              |
| Spring Web        | REST API                       |
| Spring Data JPA   | Data access                    |
| Hibernate         | ORM                            |
| Spring Security   | Authentication & authorization |
| JWT               | Stateless authentication       |
| BCrypt            | Password hashing               |
| PostgreSQL        | Relational database            |
| Flyway            | Database migrations            |
| OpenAPI / Swagger | API documentation              |
| Maven             | Build & dependency management  |
| Docker            | Database containerization      |

## Architecture

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

DTOs are used to separate the API layer from JPA entities, while centralized exception handling provides consistent API
error responses.

## Workout Model

The project separates **planned workouts** from **performed workouts**.

```text
Workout Template
"What I plan to do"

        ↓

Workout
"What I actually did"
```

Templates contain exercises, exercise order, and target sets/repetitions.

Workouts represent actual sessions and track:

* Exercises performed
* Sets and repetitions
* Weight used
* Start time
* Finish time
* Completion status
* Duration in seconds

An unfinished workout has:

```json
{
  "finished": false,
  "durationSeconds": null
}
```

When completed, the API calculates the duration from `startedAt` and `finishedAt`.

## API Documentation

Swagger UI is available when the application is running:

```text
http://localhost:8090/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8090/v3/api-docs
```

Swagger supports JWT authorization for testing protected endpoints.

## Getting Started

### Prerequisites

* Java 21
* Maven
* Docker Desktop
* Git

### 1. Clone the repository

```bash
git clone <repository-url>
cd gym-api
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

The development database runs on:

```text
Host: localhost
Port: 5432
Database: gym_db
Username: gym
```

### 3. Configure JWT secret

Set the `JWT_SECRET` environment variable.

Windows PowerShell:

```powershell
$env:JWT_SECRET="your-secure-secret"
```

The application reads the secret from:

```properties
jwt.secret=${JWT_SECRET}
```

Do not commit secrets to source control.

### 4. Run the application

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API will be available at:

```text
http://localhost:8090
```

## Database

Database schema changes are managed with Flyway.

```text
src/main/resources/db/migration/
```

Hibernate is configured with:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

This ensures that the JPA model matches the database schema without allowing Hibernate to automatically modify the
database.

## Roadmap

Planned features include:

* [x] Workout scheduling
* [x] Workout history
* [ ] Progress tracking
* [x] Unit tests
* [x] Integration/API tests
* [ ] Dockerize the application
* [ ] CI/CD pipeline with GitHub Actions
* [ ] Health checks & monitoring
* [ ] Production deployment

## Project Goals

This project focuses on practical backend development concepts including:

* REST API design
* Authentication and authorization
* Secure resource ownership
* Relational database modelling
* ORM with JPA/Hibernate
* Database versioning with Flyway
* Validation and error handling
* API documentation
* Containerized development

---

**Status:** In active development
