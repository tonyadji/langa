# Langa Backend - Reference Documentation

**Document Type:** Reference (Information-Oriented)  
**Audience:** All Developers  
**Purpose:** Complete technical reference for lookup

---

## Table of Contents

1. [REST API Reference](#rest-api-reference)
2. [Data Models](#data-models)
3. [Configuration Reference](#configuration-reference)
4. [Error Codes](#error-codes)
5. [Event Types](#event-types)

---

## REST API Reference

### Authentication

Langa has no sign-up, login or refresh endpoint: users sign in with the OpenID Connect provider and every
request carries `Authorization: Bearer <access token>`. See [docs/authentication.md](../../docs/authentication.md).

#### POST /api/dev/token/start · POST /api/dev/token/complete
**Development only** — exist only when `application.security.dev-token.enabled=true`. Return a real access
token for a user after an e-mail one-time code.

`start` request body:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| username | string | Yes | User e-mail |
| signUp | boolean | No | Create the user in the identity provider if needed |
| displayName | string | No | Display name for sign-up |

`start` response: `continuationToken`, `codeSentTo`, `codeLength`, `signUp`.

`complete` request body: `continuationToken`, `code`. Response: `accessToken`, `tokenType`, `expiresIn`.

**Error Responses:** `400-101`, `400-300` … `400-303`, `401-001`, `502-000`
(see [docs/authentication.md](../../docs/authentication.md#getting-a-token-without-the-dashboard-dev-only)).

---

### Application Endpoints

All application endpoints require authentication via `Authorization: Bearer <token>` header.

#### POST /api/applications
Create a new application.

**Request Body:**
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| name | string | Yes | Unique per owner |

**Response:** `200 OK`
| Field | Type | Description |
|-------|------|-------------|
| id | string | Application ID |
| name | string | Application name |
| key | string | Application key for ingestion |
| accountKey | string | Account key for ingestion |
| owner | string | Owner email |
| sharedWith | array | List of ShareWith objects |

---

#### GET /api/applications
List all accessible applications.

**Query Parameters:** None

**Response:** `200 OK`
```
Array of Application objects
```

**Note:** Shared applications exclude sensitive fields (key, accountKey)

---

#### GET /api/applications/{appId}/secured-details
Get full application details including secrets.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| appId | string | Application ID |

**Response:** `200 OK`
| Field | Type | Description |
|-------|------|-------------|
| id | string | Application ID |
| name | string | Application name |
| key | string | Application key |
| accountKey | string | Account key |
| secret | string | Ingestion secret |
| httpIngestionUrl | string | HTTP ingestion endpoint |
| kafkaIngestionUrl | string | Kafka broker addresses |
| kafkaTopic | string | Kafka topic name |

**Error Responses:**
- `403` - Not the application owner
- `404` - Application not found

---

#### GET /api/applications/{appId}/logs
Query application logs.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| appId | string | Application ID |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| level | string | No | - | Filter by log level |
| loggerName | string | No | - | Filter by logger name |
| startTime | string | No | - | ISO-8601 timestamp |
| endTime | string | No | - | ISO-8601 timestamp |
| page | integer | No | 0 | Page number |
| size | integer | No | 100 | Page size |

**Response:** `200 OK`
| Field | Type | Description |
|-------|------|-------------|
| content | array | Array of LogDto objects |
| totalElements | integer | Total number of logs |
| currentPage | integer | Current page number |
| pageSize | integer | Items per page |

**LogDto Structure:**
| Field | Type | Description |
|-------|------|-------------|
| message | string | Log message |
| level | string | Log level |
| loggerName | string | Logger name |
| timestamp | string | ISO-8601 timestamp |
| threadName | string | Thread name (optional) |
| stackTrace | string | Stack trace (optional) |
| mdc | object | Mapped Diagnostic Context |

---

#### GET /api/applications/{appId}/metrics
Query application metrics.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| appId | string | Application ID |

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| name | string | No | - | Filter by metric name |
| status | string | No | - | Filter by status |
| startTime | string | No | - | ISO-8601 timestamp |
| endTime | string | No | - | ISO-8601 timestamp |
| page | integer | No | 0 | Page number |
| size | integer | No | 100 | Page size |

**Response:** `200 OK` (paginated)

**MetricDto Structure:**
| Field | Type | Description |
|-------|------|-------------|
| name | string | Metric name |
| durationMillis | integer | Duration in milliseconds |
| status | string | Status (SUCCESS, FAILURE) |
| timestamp | string | ISO-8601 timestamp |
| uri | string | Request URI |
| httpMethod | string | HTTP method |
| httpStatus | integer | HTTP status code |

---

#### GET /api/applications/{appId}/usage
Get application storage usage.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| appId | string | Application ID |

**Response:** `200 OK`
| Field | Type | Description |
|-------|------|-------------|
| id | string | Application ID |
| key | string | Application key |
| name | string | Application name |
| logUsage | long | Total log bytes |
| metricUsage | long | Total metric bytes |

**Units:** Bytes

---

#### POST /api/applications/{appId}/share
Share application with user or team.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| appId | string | Application ID |

**Request Body:**
| Field | Type | Required | Values |
|-------|------|----------|--------|
| sharedWith | string | Yes | Email or team key |
| profile | string | Yes | USER or TEAM |

**Response:** `200 OK`
| Field | Type | Description |
|-------|------|-------------|
| sharedWithKey | string | Account key or team key |
| sharedWithProfile | string | USER or TEAM |
| sharedAt | string | ISO-8601 timestamp |

**Error Responses:**
- `403` - Not the owner
- `409` - Already shared
- `400` - Cannot share with self

---

#### POST /api/applications/{appId}/revoke
Revoke application access.

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| appId | string | Application ID |

**Request Body:**
| Field | Type | Required | Values |
|-------|------|----------|--------|
| sharedWith | string | Yes | Email or team key |
| profile | string | Yes | USER or TEAM |

**Response:** `204 No Content`

**Error Responses:**
- `403` - Not the owner
- `404` - Not shared with specified target

---

### Ingestion Endpoint

#### POST /api/ingestion
Ingest logs and/or metrics.

**Required Headers:**
| Header | Type | Description |
|--------|------|-------------|
| X-USER-AGENT | string | Agent identifier |
| X-APP-KEY | string | Application key |
| X-ACCOUNT-KEY | string | Account key |
| X-TIMESTAMP | string | Unix timestamp (milliseconds) |
| X-AGENT-SIGNATURE | string | HMAC-SHA256 signature (base64) |
| Content-Type | string | application/json |

**Signature Calculation:**
```
payload = X-USER-AGENT + X-APP-KEY + X-ACCOUNT-KEY + X-TIMESTAMP + request_body
signature = Base64(HMAC-SHA256(secret, payload))
```

**Request Body:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| logs | array | No | Array of LogDto objects |
| metrics | array | No | Array of MetricDto objects |

**LogDto Structure:**
| Field | Type | Required | Validation |
|-------|------|----------|------------|
| message | string | Yes | Max 1000 chars |
| level | string | Yes | - |
| loggerName | string | Yes | - |
| timestamp | string | Yes | ISO-8601 format |
| threadName | string | No | - |
| stackTrace | string | No | - |
| mdc | object | No | Key-value pairs |

**MetricDto Structure:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| name | string | Yes | Metric name |
| durationMillis | integer | Yes | Duration |
| status | string | Yes | Status |
| timestamp | string | Yes | ISO-8601 format |
| uri | string | No | Request URI |
| httpMethod | string | No | HTTP method |
| httpStatus | integer | No | HTTP status code |

**Response:** `202 Accepted`

**Error Responses:**
- `401` - Invalid signature
- `400` - Invalid timestamp or nonce reuse
- `404` - Invalid app key or account key

---

### Team Endpoints

#### POST /api/teams
Create a new team.

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| name | string | Yes |

**Response:** `201 Created`
| Field | Type | Description |
|-------|------|-------------|
| id | string | Team ID |
| name | string | Team name |
| key | string | Team key |
| role | string | Your role (OWNER) |

---

#### POST /api/teams/invite
Invite a user to a team.

**Request Body:**
| Field | Type | Required | Values |
|-------|------|----------|--------|
| teamKey | string | Yes | Team key |
| guestEmail | string | Yes | Valid email |
| role | string | Yes | MEMBER, ADMIN |

**Response:** `200 OK`

**Error Responses:**
- `403` - Not OWNER or ADMIN
- `409` - User already a member
- `404` - Team not found

---

#### GET /api/team-invitations
List pending invitations for current user.

**Response:** `200 OK`
```
Array of TeamInvitationDto
```

**TeamInvitationDto Structure:**
| Field | Type | Description |
|-------|------|-------------|
| id | string | Invitation ID |
| teamKey | string | Team key |
| teamName | string | Team name |
| hostEmail | string | Inviter email |
| role | string | Proposed role |
| expirationDate | string | ISO-8601 timestamp |

---

#### POST /api/team-invitations/accept
Accept a team invitation.

**Request Body:**
| Field | Type | Required |
|-------|------|----------|
| invitationId | string | Yes |

**Response:** `200 OK`

**Error Responses:**
- `404` - Invitation not found
- `410` - Invitation expired

---

### User Endpoints

#### GET /api/users/me
Current user. On the first call with a new access token, the user is created (or linked by e-mail to an
existing invited user).

**Response:** `200 OK`
| Field | Type | Description |
|-------|------|-------------|
| email | string | User email |
| accountKey | string | Account key |

---

### Actuator Endpoints

#### GET /actuator/health
System health check.

**Response:** `200 OK`
```json
{
  "status": "UP",
  "components": {
    "mongo": {"status": "UP"},
    "ping": {"status": "UP"}
  }
}
```

---

#### GET /actuator/info
Application information.

**Response:** `200 OK`
```json
{
  "app": {
    "name": "Langa-Backend",
    "version": "0.0.1-SNAPSHOT"
  }
}
```

---

#### GET /actuator/prometheus
Prometheus metrics.

**Response:** `200 OK` (Prometheus text format)

---

#### GET /actuator/langaMetrics
Custom Langa metrics.

**Response:** `200 OK` (JSON)

---

## Data Models

### Domain Entities

#### Application
| Field | Type | Description |
|-------|------|-------------|
| id | String | Primary key |
| name | String | Application name |
| key | String | Application key (auto-generated) |
| accountKey | String | Account key (auto-generated) |
| secret | String | Ingestion secret (auto-generated) |
| ingestionUri | String | HTTP ingestion URL |
| owner | String | Owner email |
| sharedWith | Set<ShareWith> | Sharing configuration |

**Constraints:**
- `owner + name` unique index

---

#### LogEntry
| Field | Type | Description |
|-------|------|-------------|
| appKey | String | Application key |
| accountKey | String | Account key |
| message | String | Log message |
| level | String | Log level |
| loggerName | String | Logger name |
| timestamp | Instant | Log timestamp |
| threadName | String | Thread name (optional) |
| stackTrace | String | Stack trace (optional) |
| mdc | Map<String, String> | MDC context |

**Indexes:**
- `appKey + timestamp` (compound)
- `accountKey`
- `level`
- `loggerName`

---

#### MetricEntry
| Field | Type | Description |
|-------|------|-------------|
| appKey | String | Application key |
| accountKey | String | Account key |
| name | String | Metric name |
| durationMillis | Integer | Duration |
| status | String | Status |
| timestamp | String | Timestamp |
| uri | String | Request URI |
| httpMethod | String | HTTP method |
| httpStatus | int | HTTP status code |

**Indexes:**
- `appKey + timestamp` (compound)
- `accountKey`
- `name`
- `status`

---

#### User
| Field | Type | Description |
|-------|------|-------------|
| id | String | UUID |
| email | String | User e-mail |
| accountKey | String | Unique account key (derived from the e-mail) |
| identityProvider | String | OIDC provider (`entra`, `cognito`…), empty until first sign-in |
| externalId | String | Subject of the user in the provider |
| status | UserStatus | `ACTIVE`, `CREATED` (invited), `VERIFICATION_PENDING` |

No password or token is stored: credentials live in the identity provider.

**Constraints:**
- `email` unique index
- `accountKey` unique index

---

#### Team
| Field | Type | Description |
|-------|------|-------------|
| id | String | Primary key |
| name | String | Team name |
| key | String | Unique team key |
| members | Set<TeamMember> | Team members |
| createdAt | LocalDateTime | Creation timestamp |
| createdBy | String | Creator email |

**Constraints:**
- `key` unique index

---

#### TeamMember
| Field | Type | Description |
|-------|------|-------------|
| email | String | Member email |
| role | TeamRole | Member role |
| teamKey | String | Team key |
| addedDate | LocalDateTime | Added timestamp |

---

#### TeamInvitation
| Field | Type | Description |
|-------|------|-------------|
| id | String | Primary key |
| teamKey | String | Team key |
| guestEmail | String | Invited email |
| hostEmail | String | Inviter email |
| role | TeamRole | Proposed role |
| status | InvitationStatus | Current status |
| expirationDate | LocalDateTime | Expiration |
| sentDate | LocalDateTime | Sent timestamp |

---

#### ApplicationUsage
| Field | Type | Description |
|-------|------|-------------|
| id | String | Primary key |
| appKey | String | Application key |
| totalLogBytes | long | Total log bytes |
| totalMetricBytes | long | Total metric bytes |

**Constraints:**
- `appKey` unique index

---

### Value Objects

#### ShareWith
| Field | Type | Description |
|-------|------|-------------|
| sharedWithKey | String | Account or team key |
| sharedWithProfile | SharedWithProfile | USER or TEAM |
| sharedAt | LocalDateTime | Sharing timestamp |

---

### Enumerations

#### TeamRole
- `OWNER` - Team creator, full control
- `ADMIN` - Can manage members
- `MEMBER` - Standard access

---

#### InvitationStatus
- `PENDING` - Awaiting acceptance
- `ACCEPTED` - Accepted
- `EXPIRED` - Expired
- `REVOKED` - Revoked by host

---

#### SharedWithProfile
- `USER` - Shared with individual user
- `TEAM` - Shared with team

---

#### IngestionType
- `LOG` - Log entry
- `METRIC` - Metric entry

---

## Configuration Reference

### Application Properties

#### Server Configuration
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| SERVER_PORT | int | 8080 | HTTP server port |
| BASE_URL | string | http://localhost:8080 | Base URL for emails |

---

#### Database Configuration
| Property | Type | Required | Description |
|----------|------|----------|-------------|
| MONGODB_URI | string | Yes | MongoDB connection URI |
| PERSISTENCE_PROVIDER | string | No | Persistence provider (default: MongoDB) |

---

#### Security Configuration
| Property | Type | Required | Description |
|----------|------|----------|-------------|
| AUTH_PROVIDER | string | No | Identity provider name (default `entra`) |
| AUTH_ISSUER_URI | string | Yes | Expected token issuer |
| AUTH_JWK_SET_URI | string | Yes | Signing keys of the provider |
| AUTH_AUDIENCES | string | Yes | Accepted audiences (comma separated) |
| AUTH_AUDIENCE_CLAIM | string | No | Default `aud` |
| AUTH_SCOPE_CLAIM · AUTH_REQUIRED_SCOPE | string | No | Default `scp` · `access_as_user` |
| AUTH_REQUIRED_CLAIMS | string | No | Extra `claim=value` checks |
| AUTH_SUBJECT_CLAIM | string | No | Default `oid` |
| AUTH_EMAIL_SOURCE · AUTH_EMAIL_CLAIM · AUTH_USERINFO_URI | string | No | Where to read the e-mail (`claim` or `userinfo`) |
| SECURITY_UNSECURED_ENDPOINTS | string | Yes | Comma-separated public endpoints |

Values per provider: [docs/authentication.md](../../docs/authentication.md#backend-settings).

---

#### CORS Configuration
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| CORS_ALLOWED_ORIGINS | string | * | Allowed origins |
| CORS_ALLOWED_METHODS | string | GET,POST,PUT,DELETE | Allowed methods |
| CORS_ALLOWED_HEADERS | string | * | Allowed headers |
| CORS_ALLOW_CREDENTIALS | boolean | true | Allow credentials |
| CORS_PATTERN_REGISTRY | string | /** | URL pattern |

---

#### Kafka Configuration
| Property | Type | Required | Description |
|----------|------|----------|-------------|
| KAFKA_BOOTSTRAP_SERVERS | string | No | Broker addresses |
| KAFKA_TOPIC | string | No | Ingestion topic |
| KAFKA_CONSUMER_GROUP_ID | string | No | Consumer group |
| KAFKA_AUTO_OFFSET_RESET | string | No | earliest or latest |
| KAFKA_ENABLE_AUTO_COMMIT | boolean | No | Auto-commit |
| KAFKA_LISTENER_ACK_MODE | string | No | ACK mode |

**Defaults:**
- `KAFKA_TOPIC`: langa-ingestion
- `KAFKA_AUTO_OFFSET_RESET`: earliest
- `KAFKA_ENABLE_AUTO_COMMIT`: true

---

#### Email Configuration
| Property | Type | Required | Description |
|----------|------|----------|-------------|
| MAIL_PROVIDER | string | No | Email provider |
| MAIL_HOST | string | No | SMTP host |
| MAIL_PORT | int | No | SMTP port |
| MAIL_USERNAME | string | No | SMTP username |
| MAIL_PASSWORD | string | No | SMTP password |
| MAIL_SMTP_AUTH | boolean | No | Enable auth |
| MAIL_STARTTLS_ENABLE | boolean | No | Enable STARTTLS |

**Defaults:**
- `MAIL_PORT`: 587
- `MAIL_SMTP_AUTH`: true
- `MAIL_STARTTLS_ENABLE`: true

---

#### Logging Configuration
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| LOG_LEVEL_SPRING_WEB | string | INFO | Spring Web logs |
| LOG_LEVEL_SPRING_SECURITY | string | INFO | Spring Security logs |
| LOG_LEVEL_LANGA | string | INFO | Langa application logs |
| LOG_LEVEL_KAFKA | string | WARN | Kafka logs |

---

#### File Upload Configuration
| Property | Type | Default | Description |
|----------|------|---------|-------------|
| MULTIPART_MAX_FILE_SIZE | string | 10MB | Max file size |
| MULTIPART_MAX_REQUEST_SIZE | string | 10MB | Max request size |

---

## Error Codes

### Application Errors
| Code | HTTP Status | Description |
|------|-------------|-------------|
| APPLICATION_NOT_FOUND | 404 | Application does not exist |
| APPLICATION_NAME_ALREADY_EXISTS | 409 | Duplicate name for owner |
| ACCESS_DENIED | 403 | Insufficient permissions |
| ALREADY_SHARED | 409 | Application already shared |
| CANNOT_SHARE_WITH_SELF | 400 | Owner cannot share with self |

---

### User Errors
| Code | HTTP Status | Description |
|------|-------------|-------------|
| USER_NOT_FOUND | 404 | User does not exist |
| INVALID_CREDENTIALS | 401 | Login failed |
| EMAIL_ALREADY_EXISTS | 409 | Duplicate registration |
| PASSWORD_MISMATCH | 400 | Passwords don't match |

---

### Team Errors
| Code | HTTP Status | Description |
|------|-------------|-------------|
| TEAM_NOT_FOUND | 404 | Team does not exist |
| INVITATION_NOT_FOUND | 404 | Invitation does not exist |
| INVITATION_EXPIRED | 410 | Invitation expired |
| ALREADY_MEMBER | 409 | User already a member |
| INSUFFICIENT_PRIVILEGES | 403 | Lacks required role |

---

### Ingestion Errors
| Code | HTTP Status | Description |
|------|-------------|-------------|
| INVALID_SIGNATURE | 401 | Signature validation failed |
| TIMESTAMP_EXPIRED | 400 | Request too old |
| NONCE_ALREADY_USED | 400 | Replay attack detected |

---

### Validation Errors
| Code | HTTP Status | Description |
|------|-------------|-------------|
| VALIDATION_ERROR | 400 | Field validation failed |
| INVALID_INPUT | 400 | Malformed request |

---

## Event Types

### Domain Events

#### ApplicationSharedEvent
**Trigger:** Application shared with user or team

**Payload:**
| Field | Type | Description |
|-------|------|-------------|
| applicationId | String | Application ID |
| sharedWithKey | String | Account or team key |
| sharedAt | LocalDateTime | Sharing timestamp |

**Listeners:**
- `ApplicationSharedEventListener` - Sends email notification

---

#### FirstConnectionMailEvent
**Trigger:** User signs in for the first time

**Payload:**
| Field | Type | Description |
|-------|------|-------------|
| email | String | User email |
| firstName | String | User first name |

**Listeners:**
- `AccountSetupCompleteMailListener` - Sends welcome email

---

#### InvitationAcceptedMailEvent
**Trigger:** Team invitation accepted

**Payload:**
| Field | Type | Description |
|-------|------|-------------|
| teamName | String | Team name |
| memberEmail | String | New member email |
| hostEmail | String | Inviter email |

**Listeners:**
- `InvitationAcceptedMailListener` - Notifies host

---

#### TeamInvitationAcceptedEvent
**Trigger:** Invitation accepted (team state update)

**Payload:**
| Field | Type | Description |
|-------|------|-------------|
| teamId | String | Team ID |
| memberEmail | String | New member email |
| role | TeamRole | Member role |

**Listeners:**
- `TeamInvitationAcceptedByGuestListener` - Updates guest teams
- `TeamInvitationAcceptedForHostListener` - Notifies host

---

#### TeamInvitationSentEvent
**Trigger:** Invitation sent

**Payload:**
| Field | Type | Description |
|-------|------|-------------|
| invitationId | String | Invitation ID |
| teamKey | String | Team key |
| guestEmail | String | Invited email |
| role | TeamRole | Proposed role |

**Listeners:**
- `TeamInvitationEmailListener` - Sends invitation email

---

## MongoDB Collections

### Collection Names
| Collection | Description |
|------------|-------------|
| c_applications | Application documents |
| c_log_entries | Log entries |
| c_metric_entries | Metric entries |
| c_application_usage | Usage statistics |
| c_application_nonce | Replay protection |
| c_users | User accounts |
| c_teams | Teams |
| c_team_members | Team memberships |
| c_team_invitations | Team invitations |
| c_outbox_events | Event outbox |

---

## HTTP Headers

### Authentication Headers
| Header | Required | Description |
|--------|----------|-------------|
| Authorization | Yes | Bearer {access_token} |

---

### Ingestion Headers
| Header | Required | Description |
|--------|----------|-------------|
| X-USER-AGENT | Yes | Agent identifier |
| X-APP-KEY | Yes | Application key |
| X-ACCOUNT-KEY | Yes | Account key |
| X-TIMESTAMP | Yes | Unix timestamp (ms) |
| X-AGENT-SIGNATURE | Yes | HMAC-SHA256 signature |

---

## Constants

### Log Levels
- TRACE
- DEBUG
- INFO
- WARN
- ERROR
- FATAL

### Metric Statuses
- SUCCESS
- FAILURE

### HTTP Methods
- GET
- POST
- PUT
- DELETE
- PATCH

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tonyadji/langa/issues)
- **Email:** motodigo.appvenger@gmail.com

---

**End of Reference Documentation**

For more information:
- [Specification](./01-SPECIFICATION.md) - System overview
- [Tutorial](./02-TUTORIAL.md) - Getting started
- [How-To Guides](./03-HOW-TO.md) - Task recipes
- [Explanation](./05-EXPLANATION.md) - Architecture deep dive
