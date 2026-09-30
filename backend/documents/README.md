# Langa Backend — Documentation

In-depth documentation of the [backend](../README.md), organised with the [Diátaxis](https://diataxis.fr/)
framework: each document answers one kind of need.

| Document | Kind | Read it when you want to… |
|---|---|---|
| [01 · Specification](01-SPECIFICATION.md) | Specification | Know what the system does: architecture, domain model, API contracts, security, configuration, business rules |
| [02 · Tutorial](02-TUTORIAL.md) | Learning (30–45 min) | Run the backend, get a token, create an application, send and query your first logs |
| [03 · How-to guides](03-HOW-TO.md) | Recipes | Do one task: share an application, ingest from Python or Kafka, deploy with Docker or Kubernetes… |
| [04 · Reference](04-REFERENCE.md) | Lookup | Check an endpoint, a field, an environment variable, an error code or an event |
| [05 · Explanation](05-EXPLANATION.md) | Understanding | Know *why*: hexagonal architecture, outbox, HMAC ingestion, multi-tenancy, trade-offs |

Cross-cutting guides at the repository root: [authentication](../../docs/authentication.md) ·
[deployment](../../docs/deployment.md).

## Suggested paths

| You are… | Path |
|---|---|
| **New to Langa** | Tutorial → Reference as needed |
| **Integrating an application** | [Agent README](../../agent/README.md) → How-to: Ingestion → Specification §4.3 (ingestion security) |
| **Deploying** | [Deployment](../../docs/deployment.md) → How-to: Deployment → Reference: configuration |
| **Reviewing the design** | Explanation → Specification §1 (architecture) and §6 (events) |

## At a glance

| | |
|---|---|
| Language / framework | Java 21, Spring Boot 3.5 |
| Architecture | Hexagonal (ports & adapters), DDD aggregates, domain events + outbox |
| Storage | MongoDB (TTL-based retention) |
| Messaging | Apache Kafka (optional ingestion channel) |
| Auth | OAuth2 resource server — OIDC access tokens (Microsoft Entra External ID) |
| Ingestion security | Per-application HMAC signature, timestamp window, nonce |

Found something outdated? Open an [issue](https://github.com/tonyadji/langa/issues) or a pull request
(see [CONTRIBUTING](../../CONTRIBUTING.md)).
