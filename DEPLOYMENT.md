# Deployment Guide

## CI/CD and Environments
- **Testing**: `infra/docker/testing/docker-compose.yml` spins up Postgres + the app (expects an image tag `friseur:test`). Ansible playbooks in `infra/ansible-playbooks/start-testing.yml` and `stop-testing.yml` start/stop the stack on host group `cutbyme`.
- **Production-like**: `infra/docker/main/docker-compose.yml` is the reference for a two-service stack (Postgres + app). Build or supply an image before running (see Setup guide).
- **Typical pipeline** (recommended):
  1) Run unit/integration tests: `./mvnw test`.
  2) Build jar: `./mvnw clean package -DskipTests`.
  3) Build image: `docker build -t <registry>/friseur:<tag> -f infra/docker/app/Dockerfile .`.
  4) Push image to registry.
  5) Deploy to testing via compose/Ansible; run smoke checks (`/actuator/health`).
  6) Promote to production by updating the image tag in compose and redeploying.

## Required secrets / config
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET_BASE64` (>=32 bytes before Base64); set `JWT_SECURE_COOKIE=true` in HTTPS environments.
- `SERVER_PORT` (exposed as 8080 in Dockerfile; map as needed).
- Optional: `LOGGING_FILE_NAME` if the default `/var/log/friseur/application.log` is not writable; ensure the host mount path exists (`/var/log/friseur-*` in compose files).
- If using a registry: credentials for `docker login <registry>`.

## How code goes from testing → production
1. Merge to main triggers build/test and image push.
2. Testing environment pulls the new image and runs `docker compose up -d` (Ansible helper available).
3. After validation, update production compose to the same image tag and redeploy (`docker compose pull && docker compose up -d --remove-orphans`).
4. Flyway runs automatically at app start; ensure DB backups exist before first-time deploys with schema changes.

## Manual server update (containerized)
1. SSH to the host.
2. Stop current stack (optional for zero-downtime, update in-place with `up -d --build`):
   ```bash
   docker compose -f infra/docker/main/docker-compose.yml down
   ```
3. Build/pull the new image (example):
   ```bash
   docker build -t friseur:prod -f infra/docker/app/Dockerfile .
   # or docker pull <registry>/friseur:<tag>
   ```
4. Start stack with env file in place:
   ```bash
   docker compose -f infra/docker/main/docker-compose.yml \
     --env-file infra/docker/main/.env up -d
   ```
5. Verify: `curl -f http://localhost:8080/actuator/health` and hit the UI.

## Manual server update (jar only)
1. Copy `target/friseur-0.9.jar` to the server.
2. Export env vars (DB, JWT, port).
3. Run:
   ```bash
   java -jar -XX:MaxRAMPercentage=75.0 friseur-0.9.jar
   ```
4. Use a process manager (systemd/supervisor) for restarts and log rotation.

## Rollback steps
- **Containers**: `docker compose -f infra/docker/main/docker-compose.yml down` then `docker compose ... up -d` with the previous image tag (keep older tags in the registry). If volumes are intact, data persists.
- **Jar**: restart with the prior jar build; no DB rollback is performed automatically—restore from backup if a migration broke compatibility.

## Monitoring endpoints
- `/actuator/health` (with probes), `/actuator/info`
- `/actuator/metrics` and `/actuator/prometheus` for scraping
- `/actuator/health/liveness` and `/actuator/health/readiness` (enabled via probes)
- Logs: mounted to `/var/log/friseur/` in Docker; ensure host path is writable and rotated.
