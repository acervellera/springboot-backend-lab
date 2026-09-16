# Spring Boot Backend Lab

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](#)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](#)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](#)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](#)
[![Flyway](https://img.shields.io/badge/DB%20Migrations-Flyway-CC0200)](#)
[![Security](https://img.shields.io/badge/Security-JWT-000000?logo=jsonwebtokens&logoColor=white)](#)

> Italian version: [README.it.md](README.it.md)

A portfolio-oriented REST backend built with **Java 17** and **Spring Boot**. The project brings together REST API design, relational persistence, validation, JWT security, testing, observability, OpenAPI documentation, versioned database migrations and Docker-based infrastructure.

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security
- JWT with JJWT
- BCrypt password hashing
- PostgreSQL 16
- Flyway
- Jakarta Bean Validation
- JUnit 5 / Mockito / Spring Boot Test
- Spring Boot Actuator
- OpenAPI / Swagger UI
- Maven
- Docker & Docker Compose

## Architecture

```text
Client
  |
  v
Controller
  |
  v
Service
  |
  v
Repository
  |
  v
Spring Data JPA / Hibernate
  |
  v
PostgreSQL
```

Authentication is stateless:

```text
POST /auth/login
      |
      v
Spring Security
      |
      v
AuthenticationManager
      |
      v
Database user validation
      |
      v
JWT
      |
      v
Authorization: Bearer <token>
```

## Main Features

- CRUD operations for clients
- order management
- pagination and sorting
- search by name and email
- DTO-based request/response models
- Bean Validation
- centralized exception handling
- JPA relationships
- role-based authorization (`USER`, `ADMIN`)
- JWT authentication
- BCrypt password hashing
- versioned schema migrations with Flyway
- separate Spring `dev` and `prod` profiles
- Actuator health/info endpoints
- Swagger UI / OpenAPI
- Docker multi-stage build
- PostgreSQL persistence through Docker volumes

## Security

Public endpoints:

```text
POST /auth/registrazione
POST /auth/login
GET  /actuator/health
GET  /actuator/info
GET  /swagger-ui/**
GET  /v3/api-docs/**
```

All other application endpoints require a valid JWT. Client deletion is restricted to users with the `ADMIN` role.

### How identity and roles are resolved

In this project, the JWT stores the **username** in the `sub` claim, but it does not store the user's role directly. For every authenticated request the flow is:

```text
Authorization: Bearer <token>
        |
        v
JwtAuthenticationFilter
        |
        | validates the JWT and extracts the username (sub)
        v
CustomUserDetailsService
        |
        | loads the user from the database
        v
USER / ADMIN role from the database
        |
        v
SecurityFilterChain / @PreAuthorize
```

A token generated for a `USER` identifies that username; when Spring reloads the account from the database, it obtains the `USER` role and administrative requests are denied. A token generated for the `ADMIN` account resolves to the admin username and the `ADMIN` role is loaded from the database, so administrative endpoints can be executed.

The server, however, **does not know which physical person is holding the token**. A Bearer JWT is a credential: anyone who possesses a valid `ADMIN` token is authenticated as the admin user represented by that token. A normal user cannot turn their own token into an admin token, but if they actually obtained a valid admin token they would inherit those privileges until the token expires or becomes invalid. Tokens must therefore be treated as sensitive credentials.

If a previously valid admin token stops working after `JWT_SECRET` is changed, that is expected: tokens signed with the previous secret can no longer be verified with the new key.

Client deletion is protected at two levels:

```text
DELETE /clienti/**
      |
      | SecurityFilterChain -> hasRole("ADMIN")
      v
ClienteService.elimina(...)
      |
      | @PreAuthorize("hasRole('ADMIN')")
      v
Repository / database
```

No JWT secret or database password is committed to the repository. Runtime configuration is provided through environment variables.

## Database migrations with Flyway

Hibernate no longer changes the database schema automatically. The project uses:

```properties
spring.jpa.hibernate.ddl-auto=validate
```

Hibernate therefore validates entity/schema compatibility, while Flyway owns schema evolution.

Migration files live under:

```text
src/main/resources/db/migration/
```

Example sequence:

```text
V1__init.sql
V2__add_index_clienti_email.sql
V3__...
```

On a fresh database, Flyway applies all required migrations in order. On an existing managed database, it reads `flyway_schema_history` and runs only the missing versions.

Flyway was introduced after the initial schema already existed, so the common configuration includes a baseline at version 1. New schema changes should be added as new migrations rather than editing already-applied migration files.

Real production data is not recreated by schema migrations. It is preserved through the database and its backup/restore or replication strategy. Migrations may still include reference data or data transformations required by a schema change.

## Spring profiles

The default profile is `dev`:

```properties
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

Configuration is split across:

```text
application.properties
application-dev.properties
application-prod.properties
```

In `dev`, the admin seeder is available through `@Profile("dev")`. If the configured account does not exist, it is created using `ADMIN_USERNAME` and `ADMIN_PASSWORD`.

In `prod`, the development seeder is not loaded, SQL logging is disabled and Hibernate remains in `validate` mode.

Docker Compose allows profile selection through an environment variable:

```bash
SPRING_PROFILES_ACTIVE=prod docker compose up --build -d
```

If the variable is omitted, Compose defaults to `dev`.

## Run with Docker Compose

### Requirements

Only Docker and Docker Compose are required. Java, Maven and PostgreSQL do not need to be installed locally.

### 1. Clone

```bash
git clone git@github.com:acervellera/springboot-backend-lab.git
cd springboot-backend-lab
```

### 2. Create the environment file

```bash
cp .env.example .env
```

Set at least:

```env
POSTGRES_PASSWORD=choose-a-local-password
JWT_SECRET=your-base64-secret
```

Generate a JWT secret with:

```bash
openssl rand -base64 32
```

Optional development admin seeding:

```env
ADMIN_USERNAME=admin
ADMIN_PASSWORD=choose-an-admin-password
```

If these two values are empty, no admin account is automatically created in `dev`.

### 3. Start

```bash
docker compose up --build -d
```

The application is available at:

```text
http://localhost:8080
```

PostgreSQL stays inside the Compose network and is not published on a host port by default. The application service also waits for PostgreSQL's health check before starting.

### 4. Health check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

### 5. Logs

```bash
docker compose logs -f
```

Application only:

```bash
docker compose logs -f app
```

### 6. Stop

```bash
docker compose down
```

The PostgreSQL volume is preserved. To remove the volume and its data:

```bash
docker compose down -v
```

## Authentication Example

Register:

```bash
curl -i -X POST http://localhost:8080/auth/registrazione \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo-user",
    "password": "Password123!"
  }'
```

Login:

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo-user",
    "password": "Password123!"
  }'
```

Use the returned token:

```bash
curl http://localhost:8080/clienti/trovatutti \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## API Documentation

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

## Actuator

```text
GET /actuator/health
GET /actuator/info
```

## Docker Build

The Dockerfile uses a multi-stage build:

```text
Maven + Java 17
      |
      | mvn package
      v
Spring Boot JAR
      |
      | copy
      v
Java 17 JRE runtime image
```

This makes the build reproducible and keeps Maven and source files out of the final runtime image.

## Project Structure

```text
src/
├── main/
│   ├── java/com/example/backendmid/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── db/migration/
│       ├── application.properties
│       ├── application-dev.properties
│       └── application-prod.properties
└── test/
    └── java/com/example/backendmid/

Dockerfile
compose.yaml
.env.example
pom.xml
```

## Testing

The repository includes controller, repository and integration-test examples using JUnit 5, Mockito, MockMvc and Spring Boot Test.

```bash
./mvnw test
```

## Local Development Without Docker

Provide the required database and JWT environment variables, then run:

```bash
./mvnw spring-boot:run
```

## Purpose

This project was created to consolidate practical backend engineering topics commonly encountered in Java/Spring development: layered architecture, REST design, relational persistence, authentication, authorization, validation, error handling, testing, database migrations, application profiles, observability, API documentation and containerization.

## Author

**Andrea Cervellera**  
GitHub: [@acervellera](https://github.com/acervellera)

---

Possible next steps include CI/CD with GitHub Actions, a controlled production admin bootstrap, broader integration-test coverage and additional production-configuration hardening.
