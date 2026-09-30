# Langa Backend

Spring Boot API of [Langa](../README.md): receives logs and metrics from the [agent](../agent/), stores them in
MongoDB and serves them to the [dashboard](../frontend/).

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?logo=mongodb&logoColor=white)
![Coverage gate](https://img.shields.io/badge/line%20coverage%20gate-75%25-brightgreen)

> [!NOTE]
> Work in progress — see the [roadmap](../README.md#roadmap-to-mvp).

## Contents

- [Responsibilities](#responsibilities)
- [Architecture](#architecture)
- [API at a glance](#api-at-a-glance)
- [Running locally](#running-locally)
- [Configuration](#configuration)
- [Tests and quality](#tests-and-quality)
- [Further documentation](#further-documentation)

## Responsibilities

- **Ingestion** of log and metric batches over HTTP or Kafka, authenticated with a per-application HMAC
  signature (timestamp window + nonce against replay), with payload and rate limits.
- **Applications** — creation, credentials, retention policy (MongoDB TTL), storage usage and trends.
- **Sharing** — share an application with a user or a team, revoke access.
- **Teams** — creation, e-mail invitations, acceptance.
- **Users** — created on first sign-in from the OIDC access token (Microsoft Entra External ID by default).

## Architecture

Hexagonal architecture (ports & adapters) with a Spring-free domain:

```text
com.langa.backend
├── domain/          Aggregates (Application, Team, User), value objects, domain events,
│                    use cases (@UseCase) and ports (repositories, services). No Spring, no MongoDB.
├── application/     Command bus and event listeners (side effects: e-mails, team sharing…)
├── common/          Shared kernel: errors, domain-event model, outbox contracts
└── infra/
    ├── rest/        Controllers, DTOs, error handling, ingestion limits
    ├── security/    OAuth2 resource server, identity resolution, dev-token endpoint
    ├── adapters/    MongoDB persistence, outbox, HMAC ingestion security, user/team services
    ├── kafka/       Kafka ingestion listener
    └── notifications/ E-mail templates and sender
```

**Request flow — ingestion:** `IngestionController` → `IngestionService` → `IngestionUseCase` checks the
signature through the `IngestionSecurity` port, lets the `Application` aggregate enrich the entries and update
its usage, then saves through `ApplicationRepository`.

**Side effects:** aggregates register domain events; they are written to an **outbox** collection and
dispatched every 5 s by `OutboxEventProcessor` to listeners in `application/listeners`.

Design rationale: [05-EXPLANATION.md](documents/05-EXPLANATION.md).

## API at a glance

All endpoints except ingestion and public invitation lookup require a Bearer access token.

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/ingestion` | Receive a log or metric batch (HMAC headers) |
| `GET` `POST` | `/api/applications` | List / create applications |
| `GET` `DELETE` | `/api/applications/{appId}` | Details / delete |
| `GET` | `/api/applications/{appId}/secured-details` | Ingestion credentials (owner only) |
| `GET` | `/api/applications/{appId}/logs` · `/metrics` · `/usage` | Paginated, filtered entries and usage |
| `PUT` | `/api/applications/{appId}/update-retention-policy` | Change retention |
| `POST` | `/api/applications/{appId}/share` · `/revoke-sharing` | Manage access |
| `GET` `POST` | `/api/teams` · `/api/teams/{teamId}` | Teams |
| `POST` | `/api/teams/invite` | Invite by e-mail |
| `GET` `POST` | `/api/team-invitations/{teamId}` · `/public` · `/accept` | Invitation lookup and acceptance |
| `GET` | `/api/users/me` | Current user |

Full contract: [04-REFERENCE.md](documents/04-REFERENCE.md), or Swagger UI at `/swagger-ui.html` when
`SPRINGDOC_ENABLED=true`.

## Running locally

**Prerequisites:** Java 21, a MongoDB instance, an OIDC provider ([Entra setup](../docs/authentication.md)).
Kafka is only needed for Kafka ingestion.

```bash
# Every setting comes from environment variables: start from the template
cp .env.example .env    # then fill the values

# Load the variables and run
set -a && . ./.env && set +a
./mvnw spring-boot:run
```

Alternatively, put the values in `src/main/resources/application-local.yml` (git-ignored) and run with
`-Dspring-boot.run.profiles=local`. To call the API without the dashboard, enable the
[dev-token endpoint](../docs/authentication.md#getting-a-token-without-the-dashboard-dev-only).

Health: `GET /actuator/health/liveness` and `/actuator/health/readiness`.

## Configuration

Every variable is documented in [`.env.example`](.env.example); variables without a value there are required.
Main groups: application URLs, `MONGODB_URI`, `AUTH_*` (OIDC), `CORS_*`, `INGESTION_*` limits, `KAFKA_*`,
`MAIL_*`, log levels. Production checklist: [docs/deployment.md](../docs/deployment.md).

## Tests and quality

```bash
./mvnw verify                                  # unit tests + JaCoCo coverage gate (75 % of lines)
./mvnw test-compile org.pitest:pitest-maven:mutationCoverage   # PIT mutation testing
```

- 80+ test classes covering use cases, value objects, listeners, adapters, controllers, security and
  notification templates.
- Reports: `target/site/jacoco/index.html`, `target/pit-reports/`.
- CI: [backend-ci.yml](../.github/workflows/backend-ci.yml) builds and tests the agent and the backend on
  every push; Qodana runs static analysis.

## Further documentation

The [`documents/`](documents/README.md) folder follows the Diátaxis framework:
[specification](documents/01-SPECIFICATION.md) · [tutorial](documents/02-TUTORIAL.md) ·
[how-to guides](documents/03-HOW-TO.md) · [reference](documents/04-REFERENCE.md) ·
[explanation](documents/05-EXPLANATION.md).
