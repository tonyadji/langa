# Deployment

| Component | Artifact | Pipeline |
|---|---|---|
| Agent | JAR on [Maven Central](https://central.sonatype.com/artifact/com.capricedumardi/langa-agent) | [`maven-publish.yml`](../.github/workflows/maven-publish.yml) (on GitHub release) |
| Backend | Docker image ([`backend/Dockerfile`](../backend/Dockerfile)) | [`backend-ci.yml`](../.github/workflows/backend-ci.yml) builds and tests; Railway builds and deploys the Dockerfile |
| Dashboard | Docker image (nginx, [`frontend/Dockerfile`](../frontend/Dockerfile)) | [`frontend-ci.yml`](../.github/workflows/frontend-ci.yml) builds and tests; Railway builds and deploys the Dockerfile |

Railway builds each service from the Dockerfile at the root of its folder (service root directory `backend`
or `frontend`). The configuration lives in the Railway service variables: the `AUTH_*`, `MONGODB_URI`… runtime
variables for the backend, and the `VITE_*` variables for the dashboard, which Railway passes to the Dockerfile
as build args (Vite bakes them into the static files at build time), plus `PORT=80` (nginx listens on port 80).
The production URL of the dashboard must be declared as a redirect URI of the SPA app registration.

| Environment | Dashboard |
|---|---|
| Staging | https://langa-frontend-staging.up.railway.app/ |

## Backend

The image runs the unit tests during the build (`--build-arg SKIP_TESTS=true` to skip them), runs as a
non-root user, sizes the JVM heap from the container memory limit and declares a `HEALTHCHECK` on the
liveness probe.

```bash
docker build -t langa-backend backend
docker run --env-file backend/.env -p 8080:8080 langa-backend
```

### Production checklist

1. Fill the environment from [`backend/.env.example`](../backend/.env.example) (variables without a value are required).
2. Identity provider: production redirect URIs on the SPA app registration, `AUTH_*` variables on the backend,
   `VITE_*` variables when building the dashboard (see [authentication](authentication.md)).
3. `CORS_ALLOWED_ORIGINS` and `FRONT_URL` set to the production dashboard URL.
4. `SECURITY_UNSECURED_ENDPOINTS` limited to `/api/ingestion/**,/api/team-invitations/*/public`.
5. Swagger disabled (`SPRINGDOC_ENABLED` unset or `false`); `application.security.dev-token`, `LOCAL_AUTH_ENABLED` and `DEMO_DATA_ENABLED` not enabled.
6. Probes: liveness `/actuator/health/liveness`, readiness `/actuator/health/readiness` (public, without
   details unless `MANAGEMENT_HEALTH_SHOW_DETAILS` is set).
7. Ingestion limits reviewed for the expected traffic: `INGESTION_MAX_PAYLOAD_BYTES` (default 5 MB) and
   `INGESTION_REQUESTS_PER_MINUTE` (default 1200 per application key and per backend instance, `0` to disable).
   Over the limits the endpoint answers `413` / `429` (with `Retry-After`).
8. E-mails: an SMTP server able to send the expected volume (a transactional provider rather than Gmail).

> **Single instance for now.** The outbox poller and the ingestion rate limiter are not yet coordinated
> across instances, and outbox writes are not yet atomic with aggregate writes (no Mongo transaction
> manager). See the [roadmap](../README.md#roadmap-to-mvp).

## Dashboard

See [frontend/docs/DEPLOYMENT.md](../frontend/docs/DEPLOYMENT.md) (Docker, Railway, VPS with nginx).
The `VITE_*` variables are baked in at build time.
