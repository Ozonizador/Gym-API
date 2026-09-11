# Gym API

A RESTful backend API for managing exercises, workout templates, and workout sessions, with JWT authentication and
PostgreSQL persistence.

Built as a backend-focused project using **Java 21** and **Spring Boot**, with an emphasis on clean architecture, secure
resource ownership, relational data modelling, automated testing, containerization, CI/CD, and production readiness.

## Features

* JWT-based authentication with Spring Security
* BCrypt password hashing
* User management and resource ownership
* Exercise CRUD operations
* Exercise-to-muscle-group relationships
* Reusable workout templates
* Workout CRUD operations
* Workout scheduling
* Workout history with filtering and pagination
* Automatic workout duration tracking
* Workout completion tracking
* PostgreSQL database with Flyway migrations
* Bean Validation and centralized exception handling
* OpenAPI / Swagger documentation
* Health checks with Spring Boot Actuator
* Production-oriented logging and configuration
* Dockerized application and PostgreSQL
* Automated testing with PostgreSQL Testcontainers
* GitHub Actions CI
* Automated Docker image builds

## Tech Stack

| Technology           | Purpose                                 |
|----------------------|-----------------------------------------|
| Java 21              | Programming language                    |
| Spring Boot          | Backend framework                       |
| Spring Web           | REST API                                |
| Spring Data JPA      | Data access                             |
| Hibernate            | ORM                                     |
| Spring Security      | Authentication & authorization          |
| JWT                  | Stateless authentication                |
| BCrypt               | Password hashing                        |
| PostgreSQL           | Relational database                     |
| Flyway               | Database migrations                     |
| Testcontainers       | PostgreSQL integration testing          |
| OpenAPI / Swagger    | API documentation                       |
| Spring Boot Actuator | Health checks & monitoring              |
| Maven                | Build & dependency management           |
| Docker               | Application & database containerization |
| GitHub Actions       | CI/CD automation                        |

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

Authentication uses JWT tokens with Spring Security. Protected resources verify ownership before allowing users to
access or modify their data.

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

Swagger/OpenAPI is enabled in development and disabled in the production profile.

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

### 2. Start the application

The application and PostgreSQL database can be started using Docker Compose:

```bash
docker compose up -d
```

This starts:

```text
gym-api
    ↓
Spring Boot API :8090

gym-postgres
    ↓
PostgreSQL 18 :5432
```

The PostgreSQL development database uses:

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

### 4. Run the application locally

Alternatively, the Spring Boot application can be started directly:

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

## Testing

The project contains both unit tests and integration tests.

Unit tests use JUnit and Mockito.

Integration tests use **Testcontainers with PostgreSQL**, allowing the test suite to run against a real PostgreSQL
database rather than an in-memory database.

Run the complete test suite with:

```powershell
.\mvnw.cmd clean test
```

## CI/CD

GitHub Actions automatically validates the application on every push and pull request to `master`.

The CI pipeline:

1. Starts a PostgreSQL 18 service
2. Runs the automated test suite
3. Builds the Spring Boot application
4. Builds the Docker image

```text
GitHub Push / Pull Request
            ↓
      GitHub Actions
            ↓
     PostgreSQL 18
            ↓
       Run Tests
            ↓
    Build Spring Boot JAR
            ↓
     Build Docker Image
```

Docker images are currently built for validation only and are not pushed to a container registry.

The JWT secret used by CI is stored as a GitHub Actions repository secret.

## Production Readiness

The application includes several production-oriented features:

* Health checks through Spring Boot Actuator
* Production-specific configuration
* Structured application logging
* Environment-based configuration
* JWT secret supplied through environment variables
* Hibernate schema validation
* Disabled Swagger/OpenAPI in the production profile
* Stateless JWT-based authentication
* Docker health checks for PostgreSQL

The production profile can be activated with:

```text
SPRING_PROFILES_ACTIVE=prod
```

## Deployment

### Current deployment

The application is fully containerized using Docker.

The local production-like setup consists of:

```text
Docker Compose
    │
    ├── Spring Boot API
    │       └── Port 8090
    │
    └── PostgreSQL 18
            └── Persistent Docker volume
```

### Planned AWS architecture

The following represents the planned production architecture:

```text
                         Internet
                            │
                            ▼
                 ┌─────────────────────┐
                 │ Application Load     │
                 │ Balancer             │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │ ECS Fargate         │
                 │ Spring Boot API     │
                 │ Docker container    │
                 └──────────┬──────────┘
                            │
                            ▼
                 ┌─────────────────────┐
                 │ Amazon RDS           │
                 │ PostgreSQL           │
                 └─────────────────────┘


GitHub
   │
   ▼
GitHub Actions
   │
   ├── Run tests
   ├── Build application
   ├── Build Docker image
   │
   ▼
Amazon ECR
   │
   ▼
ECS Fargate
```

The AWS architecture is currently documented as a **reference architecture** and has not been provisioned.

The planned AWS environment would use:

* **Amazon ECR** for Docker image storage
* **ECS Fargate** for running the Spring Boot container
* **Application Load Balancer** for HTTP/HTTPS traffic
* **Amazon RDS PostgreSQL** for the production database
* **HTTPS** for encrypted client communication
* **GitHub Actions** for CI/CD automation
* **OIDC** for secure GitHub Actions authentication with AWS

No AWS infrastructure is currently required to run or develop the application.

## Roadmap

### Testing & quality

* [x] Testcontainers with PostgreSQL

### Production readiness

* [x] Health checks & monitoring
* [x] Production logging
* [x] Production configuration

### Cloud & deployment

* [x] Dockerized application
* [x] PostgreSQL container
* [x] Automated Docker image build
* [ ] Production architecture diagram
* [x] Deployment documentation

### AWS deployment architecture

* [ ] Amazon RDS PostgreSQL
* [ ] Amazon ECR
* [ ] ECS Fargate
* [ ] Application Load Balancer
* [ ] HTTPS

### CI/CD

* [x] GitHub Actions CI
* [ ] GitHub Actions → AWS deployment
* [ ] OIDC authentication between GitHub Actions and AWS

### Performance

* [ ] Redis caching

### Documentation

* [x] API documentation with OpenAPI/Swagger

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
* Automated testing
* Integration testing with PostgreSQL
* Containerized development
* CI/CD automation
* Production monitoring and health checks
* AWS deployment architecture

---

**Status:** Active development
