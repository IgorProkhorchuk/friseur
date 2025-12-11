# Setup Guide

Instructions for new developers to run FrisÖr locally or in containers.

## Prerequisites
- Java 25+
- Maven 3.9+ (or use the included `mvnw`)
- PostgreSQL 16+ (local or container)
- Docker + Docker Compose (optional, for containerized runs)

## Clone the repo
```bash
git clone <repo-url>
cd friseur
```

## Environment variables
The app reads configuration from environment variables (via Spring) and supports `.env` files through `spring-dotenv`.

Minimum required:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/friseur
SPRING_DATASOURCE_USERNAME=friseur
SPRING_DATASOURCE_PASSWORD=friseur
SERVER_PORT=8080
JWT_SECRET_BASE64=base64-encoded-32B-secret
JWT_SECURE_COOKIE=false   # set true in HTTPS environments
```
Optional:
- `SPRING_PROFILES_ACTIVE` to switch profiles.
- `LOGGING_FILE_NAME` if you want a different log path than `/var/log/friseur/application.log`.
- Flyway defaults run on startup; override with `SPRING_FLYWAY_*` if needed.

## Install dependencies
```bash
./mvnw clean package -DskipTests
```
This downloads all Maven dependencies and builds `target/friseur-0.9.jar`.

## Run services locally
1. Start PostgreSQL (example with Docker):
   ```bash
   docker run -d --name friseur-db -p 5432:5432 \
     -e POSTGRES_USER=friseur -e POSTGRES_PASSWORD=friseur -e POSTGRES_DB=friseur \
     postgres:16
   ```
2. Export environment variables (or create a `.env` at repo root).
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
   Flyway will apply migrations automatically at startup.

## Docker usage
### Local/test stack
1. Build the jar: `./mvnw package -DskipTests`
2. Build an image for the test compose file:
   ```bash
   docker build -t friseur:test -f infra/docker/app/Dockerfile .
   ```
3. Start the test stack (Postgres + app on 8081):
   ```bash
   docker compose -f infra/docker/testing/docker-compose.yml \
     --env-file infra/docker/testing/.env.example up -d
   ```

### Production-like stack
- Ensure `target/friseur-0.9.jar` exists.
- Build an image tag (e.g., `friseur:prod`) with the same Dockerfile:
  ```bash
  docker build -t friseur:prod -f infra/docker/app/Dockerfile .
  ```
- Either reference that tag in `infra/docker/main/docker-compose.yml` (replace `build:` with `image:`) or adjust the `build.context` to point at `infra/docker/app` before running:
  ```bash
  docker compose -f infra/docker/main/docker-compose.yml \
    --env-file infra/docker/main/.env.example up -d
  ```
  (The provided compose file assumes the Docker build context contains `target/friseur-0.9.jar`.)

## Database migrations
- Flyway is enabled by default (`spring.flyway.enabled=true`) and runs on application startup.
- To force migrations manually against a configured DB:
  ```bash
  ./mvnw -DskipTests spring-boot:run \
    -Dspring.flyway.enabled=true \
    -Dspring.datasource.url=... \
    -Dspring.datasource.username=... \
    -Dspring.datasource.password=...
  ```

## Populate test data
- Create users through the `/register` page; passwords are hashed with BCrypt automatically.
- To promote a user to admin, add a role via SQL:
  ```sql
  INSERT INTO user_roles (user_id, role) VALUES (<user_id>, 'ROLE_ADMIN');
  ```
- Create schedules and slots through the admin UI (`/admin` then `/admin/schedule`), then book via `/slots` to generate appointment rows.
- To reset quickly during development, drop container volumes or use `TRUNCATE` on `slot`, `appointment`, and `user_roles` as needed (preserve `app_user` if you want to keep accounts).
