# Langa Backend - Technical Specification

**Version:** 0.0.1-SNAPSHOT  
**Last Updated:** December 28, 2025  
**Document Type:** Specification (Based on Implementation Analysis)

## Executive Summary

Langa Backend is a centralized observability and application monitoring platform built with Spring Boot 3.5.5 and Java 21. It provides comprehensive log and metric ingestion capabilities, multi-tenant support, team collaboration features, and role-based access control for monitoring distributed applications.

---

## 1. System Architecture

### 1.1 Architecture Style
- **Pattern:** Clean Architecture / Hexagonal Architecture
- **Framework:** Spring Boot 3.5.5
- **Language:** Java 21
- **Build Tool:** Maven

### 1.2 Layer Structure

```
├── Application Layer (application/listeners)
│   └── Event-driven listeners for domain events
├── Domain Layer (domain)
│   ├── applications/     - Application management domain
│   ├── teams/           - Team collaboration domain
│   └── users/           - User management domain
├── Infrastructure Layer (infra)
│   ├── adapters/        - Persistence implementations
│   ├── rest/            - REST API controllers
│   ├── kafka/           - Kafka consumers
│   ├── security/        - Security configuration
│   └── notifications/   - Email notifications
└── Common (common)
    ├── annotations/     - Custom annotations
    ├── eda/            - Event-driven architecture support
    └── model/          - Shared models
```

### 1.3 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Runtime | Java | 21 |
| Framework | Spring Boot | 3.5.5 |
| Database | MongoDB | - |
| Message Broker | Apache Kafka | - |
| Security | Spring Security + JWT | - |
| Validation | Jakarta Validation | 3.x |
| Testing | JUnit 5 + Mockito | - |
| Code Coverage | JaCoCo | 0.8.13 |
| API Documentation | SpringDoc OpenAPI | 2.8.13 |
| Email | Spring Mail | - |

---

## 2. Core Domain Model

### 2.1 Applications Domain

#### Application Entity
**Purpose:** Represents a monitored application in the system.

**Attributes:**
- `id` (String) - Unique identifier
- `name` (String) - Application name (must be unique per owner)
- `key` (String) - Application key for ingestion (auto-generated)
- `accountKey` (String) - Account key for multi-tenancy
- `secret` (String) - Secret for signature validation
- `ingestionUri` (String) - HTTP ingestion endpoint
- `owner` (String) - Email of the application owner
- `sharedWith` (Set<ShareWith>) - Users/teams with access

**Operations:**
- `createNew(name, accountKey, owner)` - Factory method to create a new application
- `populate(...)` - Reconstitute from persistence
- `populateSecured(...)` - Reconstitute with sensitive data
- `createLogEntries(logs)` - Associate logs with this application
- `createMetricEntries(metrics)` - Associate metrics with this application
- `checkOwnership(username)` - Verify owner access
- `authorizedToAccess(username, accountKeys)` - Verify any access
- `shareWith(accountOrTeamKey, profile)` - Share with user or team
- `revokeSharing(accountOrTeamKey)` - Remove access

#### LogEntry Value Object
**Purpose:** Represents a log entry ingested from an application.

**Attributes:**
- `appKey` (String) - Application identifier
- `accountKey` (String) - Account identifier
- `message` (String) - Log message
- `level` (String) - Log level (INFO, WARN, ERROR, etc.)
- `loggerName` (String) - Logger name
- `timestamp` (Instant) - Log timestamp
- `threadName` (String) - Thread name (optional)
- `stackTrace` (String) - Stack trace for errors (optional)
- `mdc` (Map<String, String>) - Mapped Diagnostic Context (optional)

#### MetricEntry Value Object
**Purpose:** Represents a metric entry ingested from an application.

**Attributes:**
- `appKey` (String) - Application identifier
- `accountKey` (String) - Account identifier
- `name` (String) - Metric name
- `durationMillis` (Integer) - Duration in milliseconds
- `status` (String) - Status (SUCCESS, FAILURE, etc.)
- `timestamp` (String) - Metric timestamp
- `uri` (String) - Request URI (for HTTP metrics)
- `httpMethod` (String) - HTTP method
- `httpStatus` (int) - HTTP status code

#### ApplicationUsage Record
**Purpose:** Tracks storage usage per application.

**Backend Format (API Response):**
- `id` (String) - Unique identifier
- `key` (String) - Application key
- `name` (String) - Application name
- `logUsage` (long) - Total bytes used by logs
- `metricUsage` (long) - Total bytes used by metrics

**Frontend Type (After Mapping):**
- `id` (String) - Unique identifier
- `appKey` (String) - Application key (mapped from backend `key`)
- `totalLogBytes` (long) - Total bytes used by logs (mapped from backend `logUsage`)
- `totalMetricBytes` (long) - Total bytes used by metrics (mapped from backend `metricUsage`)

**Operations:**
- `increaseLogBytes(bytes)` - Add log storage usage
- `increaseTotalMetricBytes(bytes)` - Add metric storage usage

**Note:** The frontend automatically maps the backend response format to the frontend type in `useApplicationUsage` hook.

#### ShareWith Value Object
**Purpose:** Represents sharing configuration.

**Attributes:**
- `sharedWithKey` (String) - User account key or team key
- `sharedWithProfile` (SharedWithProfile) - USER or TEAM
- `sharedAt` (LocalDateTime) - When sharing was granted

**Enums:**
- `SharedWithProfile`: USER, TEAM
- `IngestionType`: LOG, METRIC

### 2.2 Users Domain

#### User Entity
**Purpose:** Represents a system user.

**Attributes:**
- `email` (String) - User email (unique identifier)
- `password` (String) - Encrypted password
- `accountKey` (String) - Unique account key
- `role` (String) - User role
- `firstConnection` (boolean) - First login flag
- `registrationDate` (LocalDateTime) - Registration timestamp

#### RefreshToken Entity
**Purpose:** Manages JWT refresh tokens.

**Attributes:**
- `id` (String) - Token identifier
- `token` (String) - Refresh token value
- `username` (String) - Associated user
- `expirationDate` (LocalDateTime) - Expiration timestamp

### 2.3 Teams Domain

#### Team Entity
**Purpose:** Enables collaboration by grouping users.

**Attributes:**
- `id` (String) - Unique identifier
- `name` (String) - Team name
- `key` (String) - Unique team key
- `members` (Set<TeamMember>) - Team members
- `createdAt` (LocalDateTime) - Creation timestamp
- `createdBy` (String) - Creator email

**Operations:**
- `createNew(name, creatorEmail)` - Create a new team
- `addMember(email, role)` - Add a member with role
- `removeMember(email)` - Remove a member
- `updateMemberRole(email, newRole)` - Change member role
- `hasMember(email)` - Check membership
- `isOwner(email)` - Check ownership
- `isAdmin(email)` - Check admin privileges

#### TeamMember Value Object
**Attributes:**
- `email` (String) - Member email
- `role` (TeamRole) - Member role
- `teamKey` (String) - Team identifier
- `addedDate` (LocalDateTime) - When added

**TeamRole Enum:**
- `OWNER` - Team creator with full control
- `ADMIN` - Can manage members and settings
- `MEMBER` - Standard access

#### TeamInvitation Entity
**Purpose:** Manages team invitation lifecycle.

**Attributes:**
- `id` (String) - Invitation identifier
- `teamKey` (String) - Target team
- `guestEmail` (String) - Invited user
- `hostEmail` (String) - Inviter
- `role` (TeamRole) - Proposed role
- `status` (InvitationStatus) - Current status
- `expirationDate` (LocalDateTime) - Expiration
- `sentDate` (LocalDateTime) - Sent timestamp

---

## 3. API Specification

### 3.1 Authentication & Authorization

**Base Path:** `/api/auth`

#### POST /register
**Purpose:** Register a new user account.

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "SecurePass123!",
  "confirmationPassword": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
"User registered"
```

**Validation Rules:**
- Email format required
- Password minimum length
- Passwords must match

#### POST /login
**Purpose:** Authenticate and obtain JWT tokens.

**Request Body:**
```json
{
  "username": "user@example.com",
  "password": "SecurePass123!"
}
```

**Response:** `200 OK`
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "refresh_token_value",
  "email": "user@example.com"
}
```

#### POST /refresh
**Purpose:** Refresh an expired access token.

**Request Body:**
```json
{
  "refreshToken": "refresh_token_value"
}
```

**Response:** `200 OK` (same structure as login)

### 3.2 Application Management

**Base Path:** `/api/applications`  
**Authentication:** Required (Bearer JWT)

#### POST /
**Purpose:** Create a new monitored application.

**Request Body:**
```json
{
  "name": "My Application"
}
```

**Response:** `200 OK`
```json
{
  "id": "app_id_123",
  "name": "My Application",
  "key": "app_key_xyz",
  "accountKey": "account_abc",
  "owner": "user@example.com",
  "sharedWith": []
}
```

**Business Rules:**
- Application name must be unique per owner
- Automatically generates `key`, `accountKey`, `secret`, and `ingestionUri`

#### GET /
**Purpose:** List all applications accessible to the user.

**Response:** `200 OK`
```json
[
  {
    "id": "app_id_123",
    "name": "My Application",
    "key": "app_key_xyz",
    "accountKey": "account_abc",
    "owner": "user@example.com",
    "sharedWith": []
  }
]
```

**Notes:**
- Returns owned applications and applications shared with the user
- Shared applications have restricted fields (no keys)

#### GET /{appId}/secured-details
**Purpose:** Get full application details including secrets.

**Response:** `200 OK`
```json
{
  "id": "app_id_123",
  "name": "My Application",
  "key": "app_key_xyz",
  "accountKey": "account_abc",
  "secret": "secret_value",
  "httpIngestionUrl": "http://localhost:8080/api/ingestion",
  "kafkaIngestionUrl": "localhost:9092",
  "kafkaTopic": "langa-ingestion"
}
```

**Authorization:**
- Requires ownership of the application

#### GET /{appId}/logs
**Purpose:** Retrieve logs for an application.

**Query Parameters:**
- `level` (optional) - Filter by log level
- `loggerName` (optional) - Filter by logger name
- `startTime` (optional) - ISO-8601 timestamp
- `endTime` (optional) - ISO-8601 timestamp
- `page` (default: 0) - Page number
- `size` (default: 100) - Page size

**Response:** `200 OK`
```json
{
  "content": [
    {
      "message": "Application started",
      "level": "INFO",
      "loggerName": "com.example.MyApp",
      "timestamp": "2025-12-28T10:00:00Z",
      "threadName": "main",
      "stackTrace": null,
      "mdc": {}
    }
  ],
  "totalElements": 1234,
  "currentPage": 0,
  "pageSize": 100
}
```

#### GET /{appId}/metrics
**Purpose:** Retrieve metrics for an application.

**Query Parameters:**
- `name` (optional) - Filter by metric name
- `status` (optional) - Filter by status
- `startTime` (optional) - ISO-8601 timestamp
- `endTime` (optional) - ISO-8601 timestamp
- `page` (default: 0) - Page number
- `size` (default: 100) - Page size

**Response:** `200 OK` (similar paginated structure)

#### GET /{appId}/usage
**Purpose:** Get storage usage statistics.

**Response:** `200 OK`
```json
{
  "id": "app_id_123",
  "key": "app_key_xyz",
  "name": "My Application",
  "logUsage": 1048576,
  "metricUsage": 524288
}
```

**Units:** Bytes

#### POST /{appId}/share
**Purpose:** Share an application with a user or team.

**Request Body:**
```json
{
  "sharedWith": "user@example.com",
  "profile": "USER"
}
```

or

```json
{
  "sharedWith": "team_key_xyz",
  "profile": "TEAM"
}
```

**Response:** `200 OK`
```json
{
  "sharedWithKey": "account_key_or_team_key",
  "sharedWithProfile": "USER",
  "sharedAt": "2025-12-28T10:00:00"
}
```

**Business Rules:**
- Only owners can share
- Cannot share with self
- Cannot share if already shared

#### POST /{appId}/revoke
**Purpose:** Revoke application access.

**Request Body:**
```json
{
  "sharedWith": "user@example.com",
  "profile": "USER"
}
```

**Response:** `204 No Content`

### 3.3 Data Ingestion

**Base Path:** `/api/ingestion`  
**Authentication:** Header-based signature validation

#### POST /
**Purpose:** Ingest logs and/or metrics from applications.

**Required Headers:**
- `X-USER-AGENT` - Agent identifier
- `X-AGENT-SIGNATURE` - HMAC signature
- `X-APP-KEY` - Application key
- `X-ACCOUNT-KEY` - Account key
- `X-TIMESTAMP` - Request timestamp (nonce for replay protection)

**Request Body:**
```json
{
  "logs": [
    {
      "message": "Application started",
      "level": "INFO",
      "loggerName": "com.example.MyApp",
      "timestamp": "2025-12-28T10:00:00Z",
      "threadName": "main",
      "stackTrace": null,
      "mdc": {}
    }
  ],
  "metrics": [
    {
      "name": "http.request",
      "durationMillis": 125,
      "status": "SUCCESS",
      "timestamp": "2025-12-28T10:00:00Z",
      "uri": "/api/users",
      "httpMethod": "GET",
      "httpStatus": 200
    }
  ]
}
```

**Response:** `202 Accepted`

**Security:**
- Signature validation using app secret
- Nonce-based replay protection
- Timestamp expiration validation

**Supported Ingestion Methods:**
1. **HTTP:** Via this endpoint
2. **Kafka:** Publish to `langa-ingestion` topic

### 3.4 Team Management

**Base Path:** `/api/teams`  
**Authentication:** Required (Bearer JWT)

#### POST /
**Purpose:** Create a new team.

**Request Body:**
```json
{
  "name": "Development Team"
}
```

**Response:** `201 Created`
```json
{
  "id": "team_id_123",
  "name": "Development Team",
  "key": "team_key_xyz",
  "role": "OWNER"
}
```

**Base Path:** `/api/teams/invite`

#### POST /
**Purpose:** Invite a user to a team.

**Request Body:**
```json
{
  "teamKey": "team_key_xyz",
  "guestEmail": "member@example.com",
  "role": "MEMBER"
}
```

**Response:** `200 OK`

**Base Path:** `/api/team-invitations`

#### GET /
**Purpose:** List pending invitations for the current user.

**Response:** `200 OK`
```json
[
  {
    "id": "invitation_id",
    "teamKey": "team_key_xyz",
    "teamName": "Development Team",
    "hostEmail": "owner@example.com",
    "role": "MEMBER",
    "expirationDate": "2026-01-28T10:00:00"
  }
]
```

#### POST /accept
**Purpose:** Accept a team invitation.

**Request Body:**
```json
{
  "invitationId": "invitation_id"
}
```

**Response:** `200 OK`

### 3.5 User Profile

**Base Path:** `/api/users`  
**Authentication:** Required (Bearer JWT)

#### GET /profile
**Purpose:** Get current user profile.

**Response:** `200 OK`
```json
{
  "email": "user@example.com",
  "accountKey": "account_key_abc",
  "firstConnection": false
}
```

#### POST /complete-setup
**Purpose:** Complete first-time setup.

**Request Body:**
```json
{
  "hasCompletedSetup": true
}
```

**Response:** `200 OK`

---

## 4. Security Model

### 4.1 Authentication

**Mechanism:** JWT (JSON Web Tokens)

**Token Types:**
1. **Access Token**
   - Lifetime: Configurable via `JWT_EXPIRATION` (default: 1 hour)
   - Used for API authentication
   - Passed as `Authorization: Bearer <token>`

2. **Refresh Token**
   - Lifetime: Configurable via `JWT_REFRESH_TOKEN_EXPIRATION` (default: 7 days)
   - Used to obtain new access tokens
   - Stored in database with user association

**JWT Configuration:**
- Algorithm: Configured via `JWT_KID`
- Secret: `JWT_KEY` environment variable
- Issuer: Langa Backend

### 4.2 Authorization

**Access Control Levels:**
1. **Public Endpoints**
   - `/api/auth/register`
   - `/api/auth/login`
   - `/api/auth/refresh`
   - Actuator endpoints (health, metrics)

2. **Authenticated Endpoints**
   - All other `/api/*` endpoints
   - Require valid JWT access token

3. **Resource-Level Authorization**
   - Applications: Owner or shared access
   - Teams: Member or admin privileges
   - Logs/Metrics: Application access required

### 4.3 Ingestion Security

**HMAC Signature Validation:**
```
Signature = HMAC-SHA256(
  secret,
  X-USER-AGENT + X-APP-KEY + X-ACCOUNT-KEY + X-TIMESTAMP + request_body
)
```

**Protection Mechanisms:**
- Timestamp validation (prevents old requests)
- Nonce storage (prevents replay attacks)
- Secret-based signature (verifies authenticity)

### 4.4 CORS Configuration

Configurable via environment:
- `CORS_ALLOWED_ORIGINS` - Allowed origins (e.g., `http://localhost:3000`)
- `CORS_ALLOWED_METHODS` - Allowed HTTP methods
- `CORS_ALLOWED_HEADERS` - Allowed headers
- `CORS_ALLOW_CREDENTIALS` - Allow credentials (true/false)

---

## 5. Data Persistence

### 5.1 Database

**Technology:** MongoDB

**Collections:**
- `c_applications` - Application documents
- `c_log_entries` - Log entry documents
- `c_metric_entries` - Metric entry documents
- `c_application_usage` - Usage statistics
- `c_application_nonce` - Replay protection nonces
- `c_users` - User documents
- `c_refresh_tokens` - Refresh token documents
- `c_teams` - Team documents
- `c_team_members` - Team membership documents
- `c_team_invitations` - Team invitation documents
- `c_outbox_events` - Domain event outbox

**Connection:**
- URI: `MONGODB_URI` environment variable
- Provider: `PERSISTENCE_PROVIDER` (default: MongoDB)

### 5.2 Indexing Strategy

**Applications:**
- `key` (unique)
- `accountKey`
- `owner`
- `owner + name` (compound, unique)

**Logs:**
- `appKey + timestamp` (compound)
- `accountKey`
- `level`
- `loggerName`

**Metrics:**
- `appKey + timestamp` (compound)
- `accountKey`
- `name`
- `status`

**Users:**
- `email` (unique)
- `accountKey` (unique)

**Teams:**
- `key` (unique)
- `createdBy`

---

## 6. Event-Driven Architecture

### 6.1 Domain Events

**Pattern:** Event Sourcing with Outbox Pattern

**Event Types:**
1. **ApplicationSharedEvent**
   - Triggered when an application is shared
   - Payload: `appId`, `sharedWithKey`, `sharedAt`

2. **FirstConnectionMailEvent**
   - Triggered on user first login
   - Payload: `email`, `firstName`

3. **InvitationAcceptedMailEvent**
   - Triggered when team invitation is accepted
   - Payload: `teamName`, `memberEmail`

4. **TeamInvitationAcceptedEvent**
   - Triggered for team state update
   - Payload: `teamId`, `memberEmail`, `role`

5. **TeamInvitationSentEvent**
   - Triggered when invitation is sent
   - Payload: `invitationId`, `teamKey`, `guestEmail`

### 6.2 Event Listeners

**Application Listeners:**
- `ApplicationSharedEventListener` - Sends email notifications

**User Listeners:**
- `AccountSetupCompleteMailListener` - Sends welcome email

**Team Listeners:**
- `TeamInvitationEmailListener` - Sends invitation emails
- `InvitationAcceptedMailListener` - Sends acceptance notifications
- `TeamInvitationAcceptedByGuestListener` - Updates guest team membership
- `TeamInvitationAcceptedForHostListener` - Notifies host

### 6.3 Kafka Integration

**Consumer Configuration:**
- Topic: `KAFKA_TOPIC` (configurable)
- Group ID: `KAFKA_CONSUMER_GROUP_ID`
- Auto Offset Reset: `KAFKA_AUTO_OFFSET_RESET`
- ACK Mode: `KAFKA_LISTENER_ACK_MODE`

**Consumer:** `IngestionConsumer`
- Consumes ingestion requests from Kafka
- Same processing as HTTP ingestion
- Asynchronous processing

---

## 7. Configuration

### 7.1 Environment Variables

**Application:**
- `APP_NAME` - Application name
- `BASE_URL` - Base URL for links in emails
- `SERVER_PORT` - HTTP server port (default: 8080)
- `AGENT_VERSION` - Expected agent version

**Database:**
- `MONGODB_URI` - MongoDB connection string

**Kafka:**
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka broker addresses
- `KAFKA_TOPIC` - Ingestion topic name
- `KAFKA_CONSUMER_GROUP_ID` - Consumer group
- `KAFKA_AUTO_OFFSET_RESET` - earliest/latest
- `KAFKA_ENABLE_AUTO_COMMIT` - true/false

**Security:**
- `JWT_KEY` - JWT signing secret
- `JWT_KID` - Key ID
- `JWT_EXPIRATION` - Access token lifetime (milliseconds)
- `JWT_REFRESH_TOKEN_EXPIRATION` - Refresh token lifetime (milliseconds)
- `SECURITY_UNSECURED_ENDPOINTS` - Comma-separated list of public endpoints

**CORS:**
- `CORS_ALLOWED_ORIGINS` - Allowed origins
- `CORS_ALLOWED_METHODS` - Allowed HTTP methods
- `CORS_ALLOWED_HEADERS` - Allowed headers
- `CORS_ALLOW_CREDENTIALS` - true/false
- `CORS_PATTERN_REGISTRY` - URL pattern for CORS

**Email:**
- `MAIL_PROVIDER` - Email provider (SMTP)
- `MAIL_HOST` - SMTP server host
- `MAIL_PORT` - SMTP server port
- `MAIL_USERNAME` - SMTP username
- `MAIL_PASSWORD` - SMTP password
- `MAIL_SMTP_AUTH` - Enable SMTP auth (true/false)
- `MAIL_STARTTLS_ENABLE` - Enable STARTTLS (true/false)

**Logging:**
- `LOG_LEVEL_SPRING_WEB` - Spring Web log level
- `LOG_LEVEL_SPRING_SECURITY` - Spring Security log level
- `LOG_LEVEL_LANGA` - Langa application log level
- `LOG_LEVEL_KAFKA` - Kafka log level

**File Upload:**
- `MULTIPART_MAX_FILE_SIZE` - Maximum file size
- `MULTIPART_MAX_REQUEST_SIZE` - Maximum request size

### 7.2 Application Profiles

**Default Profile:** Production-ready configuration

**Test Profile (`application-test.yml`):**
- Used for integration tests
- In-memory or test database configurations

---

## 8. Use Cases

### 8.1 Application Management Use Cases

| Use Case | Description | Actor |
|----------|-------------|-------|
| Create Application | Register a new application for monitoring | Application Owner |
| List Applications | View owned and shared applications | User |
| Get Application Details | Retrieve full application configuration | Application Owner |
| Share Application | Grant read access to user or team | Application Owner |
| Revoke Access | Remove shared access | Application Owner |
| Get Usage Statistics | View storage consumption | Application Owner |

### 8.2 Observability Use Cases

| Use Case | Description | Actor |
|----------|-------------|-------|
| Ingest Logs | Send log entries to Langa | Application Agent |
| Ingest Metrics | Send metrics to Langa | Application Agent |
| Query Logs | Search and filter logs | User with Access |
| Query Metrics | Search and filter metrics | User with Access |
| Filter by Time Range | Query data within time window | User with Access |
| Filter by Level | Query logs by severity | User with Access |

### 8.3 Team Collaboration Use Cases

| Use Case | Description | Actor |
|----------|-------------|-------|
| Create Team | Start a new collaboration team | User |
| Invite Member | Send team invitation | Team Owner/Admin |
| Accept Invitation | Join a team | Invited User |
| Manage Members | Add/remove/update roles | Team Owner/Admin |
| Share with Team | Grant team access to application | Application Owner |

### 8.4 User Management Use Cases

| Use Case | Description | Actor |
|----------|-------------|-------|
| Register | Create a new account | Anonymous |
| Login | Authenticate and obtain tokens | Registered User |
| Refresh Token | Renew access token | User |
| Complete Setup | Finish onboarding | First-time User |
| View Profile | See account information | User |

---

## 9. Business Rules

### 9.1 Application Rules

1. Application names must be unique per owner
2. Only owners can:
   - View secrets and full configuration
   - Share the application
   - Revoke access
3. Shared users can:
   - View logs and metrics
   - View basic application info (no keys/secrets)
4. Applications cannot be shared with the owner
5. Applications cannot be deleted (no delete operation defined)

### 9.2 Ingestion Rules

1. All ingestion requests must include valid signature
2. Timestamps must be within acceptable time window
3. Nonces cannot be reused (replay protection)
4. Both HTTP and Kafka ingestion are supported
5. Usage statistics are updated on each ingestion

### 9.3 Team Rules

1. Team names are not enforced unique (unlike applications)
2. Team creator automatically becomes OWNER
3. Only OWNER and ADMIN can invite members
4. Only OWNER can remove ADMIN members
5. OWNER cannot be removed from team
6. OWNER role cannot be changed
7. Invitations expire after configured duration
8. Invitations can only be accepted once

### 9.4 Security Rules

1. All API endpoints (except `/api/auth/*`) require authentication
2. JWT tokens expire and must be refreshed
3. Refresh tokens are single-use
4. First-time users must complete setup
5. Passwords must meet minimum strength requirements

---

## 10. Error Handling

### 10.1 Error Response Format

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable error message",
  "details": {
    "field": "validation error message"
  },
  "timestamp": "2025-12-28T10:00:00Z"
}
```

### 10.2 Error Codes

**Application Errors:**
- `APPLICATION_NOT_FOUND` - Application does not exist
- `APPLICATION_NAME_ALREADY_EXISTS` - Duplicate name for owner
- `ACCESS_DENIED` - Insufficient permissions
- `ALREADY_SHARED` - Application already shared with target
- `CANNOT_SHARE_WITH_SELF` - Owner cannot share with themselves

**User Errors:**
- `USER_NOT_FOUND` - User does not exist
- `INVALID_CREDENTIALS` - Login failed
- `EMAIL_ALREADY_EXISTS` - Duplicate registration
- `PASSWORD_MISMATCH` - Passwords don't match

**Team Errors:**
- `TEAM_NOT_FOUND` - Team does not exist
- `INVITATION_NOT_FOUND` - Invitation does not exist
- `INVITATION_EXPIRED` - Invitation has expired
- `ALREADY_MEMBER` - User is already a team member
- `INSUFFICIENT_PRIVILEGES` - User lacks required role

**Ingestion Errors:**
- `INVALID_SIGNATURE` - Signature validation failed
- `TIMESTAMP_EXPIRED` - Request timestamp too old
- `NONCE_ALREADY_USED` - Replay attack detected

### 10.3 HTTP Status Codes

| Status | Usage |
|--------|-------|
| 200 OK | Successful GET, PUT |
| 201 Created | Successful POST (resource creation) |
| 202 Accepted | Accepted for async processing |
| 204 No Content | Successful DELETE |
| 400 Bad Request | Validation error |
| 401 Unauthorized | Missing/invalid authentication |
| 403 Forbidden | Insufficient permissions |
| 404 Not Found | Resource not found |
| 500 Internal Server Error | Unexpected error |

---

## 11. Non-Functional Requirements

### 11.1 Performance

**Ingestion:**
- Target: Handle 10,000+ events/second via Kafka
- HTTP ingestion accepted within 200ms
- Asynchronous processing for high throughput

**Query:**
- Log/metric queries return within 2 seconds
- Pagination support for large result sets
- Default page size: 100 items

### 11.2 Scalability

**Horizontal Scaling:**
- Stateless application servers (JWT-based auth)
- MongoDB supports sharding
- Kafka consumers can be partitioned

**Vertical Scaling:**
- Java 21 virtual threads support (if configured)
- Connection pooling for database
- Caching layer can be added

### 11.3 Reliability

**Data Durability:**
- MongoDB persistence
- Kafka message durability
- Outbox pattern for reliable event publishing

**High Availability:**
- Stateless architecture
- Can run multiple instances behind load balancer
- MongoDB replica sets for database HA

### 11.4 Observability

**Metrics:**
- Spring Boot Actuator endpoints
- Custom metrics via `/actuator/langaMetrics`
- JaCoCo test coverage reports

**Monitoring:**
- Health check: `/actuator/health`
- Application info: `/actuator/info`
- Logs configurable per package

---

## 12. Deployment

### 12.1 Prerequisites

1. **Java Runtime Environment:** JDK 21
2. **MongoDB:** Running instance or cloud service
3. **Apache Kafka:** Running cluster (optional, for Kafka ingestion)
4. **SMTP Server:** For email notifications

### 12.2 Build & Package

```bash
# Build
./mvnw clean package

# Run tests
./mvnw test

# Skip tests
./mvnw package -DskipTests

# Generate coverage report
./mvnw test jacoco:report
```

**Artifacts:**
- JAR: `target/langa-backend-0.0.1-SNAPSHOT.jar`
- Coverage Report: `target/site/jacoco/index.html`

### 12.3 Running

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Using JAR
java -jar target/langa-backend-0.0.1-SNAPSHOT.jar

# With environment file
java -jar target/langa-backend-0.0.1-SNAPSHOT.jar --spring.config.location=application.properties
```

### 12.4 Container Deployment

**Docker:**
```dockerfile
FROM eclipse-temurin:21-jre
COPY target/langa-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Environment Variables:**
- All configuration via environment variables
- No hardcoded secrets in image

---

## 13. Testing

### 13.1 Test Coverage

**Current Coverage:** 79 tests, 0 failures

**Test Categories:**
- Unit Tests: Domain logic, use cases
- Integration Tests: Controllers, repositories
- Security Tests: Authentication, authorization

**Coverage Tool:** JaCoCo 0.8.13

### 13.2 Test Structure

```
src/test/java
├── common/utils          - Utility tests
├── domain
│   ├── applications      - Application domain tests
│   ├── teams            - Team domain tests
│   └── users            - User domain tests
└── infra
    ├── rest             - Controller tests
    └── security         - Security tests
```

---

## 14. API Documentation

**Tool:** SpringDoc OpenAPI (Swagger UI)

**Endpoints:**
- Swagger UI: `/swagger-ui.html`
- OpenAPI Spec: `/v3/api-docs`

**Auto-Generated:**
- All REST endpoints documented
- Request/response schemas
- Authentication requirements

---

## 15. Extensibility Points

### 15.1 Plugin Architecture

**Custom Annotations:**
- `@UseCase` - Marks use case classes
- `@DomainEventType` - Registers event types

**Event System:**
- Implement `DomainEvent` interface
- Register in `EventTypeRegistry`
- Add listener in application layer

### 15.2 Storage Providers

**Current:** MongoDB

**Extension Point:** Repository interfaces
- Implement `ApplicationRepository`
- Implement `LogEntryRepository`
- Implement `MetricEntryRepository`
- Configure via `PERSISTENCE_PROVIDER`

### 15.3 Notification Providers

**Current:** Email via Spring Mail

**Extension Point:** Notification service interface
- Implement notification sender
- Configure via `MAIL_PROVIDER`

---

## 16. Known Limitations

1. **No Delete Operations:**
   - Applications cannot be deleted
   - Users cannot be deleted
   - Teams cannot be deleted

2. **Single Persistence Provider:**
   - Only MongoDB is implemented
   - SQL databases not supported

3. **Limited Query Capabilities:**
   - Basic filtering only
   - No full-text search
   - No aggregation queries

4. **Ingestion Validation:**
   - Minimal validation of log/metric content
   - No schema validation

5. **User Management:**
   - No password reset flow
   - No email verification
   - No multi-factor authentication

---

## 17. Future Enhancements (Potential)

1. **Advanced Querying:**
   - Full-text search (Elasticsearch integration)
   - Advanced filtering and aggregation
   - Saved queries and dashboards

2. **Alerting:**
   - Threshold-based alerts
   - Anomaly detection
   - Multi-channel notifications (Slack, PagerDuty)

3. **Data Retention:**
   - Configurable retention policies
   - Archival to cold storage
   - Data export capabilities

4. **Enhanced Security:**
   - OAuth2 integration
   - SSO support
   - API key management

5. **Visualization:**
   - Built-in dashboard builder
   - Real-time streaming views
   - Custom metric charts

---

## Appendix A: Glossary

**Account Key:** Unique identifier for a user account, used for multi-tenancy.

**Application Key:** Unique identifier for a monitored application, used in ingestion.

**Ingestion:** The process of sending logs/metrics from an application to Langa.

**MDC:** Mapped Diagnostic Context - contextual information attached to log entries.

**Nonce:** Number used once - prevents replay attacks in ingestion.

**Outbox Pattern:** Ensures reliable event publishing using database transactions.

**Secured Application:** Application data including secrets and ingestion configuration.

**Share With:** Granting read access to logs/metrics to another user or team.

**Use Case:** Service layer component implementing business logic.

---

## Appendix B: References

- **Spring Boot Documentation:** https://spring.io/projects/spring-boot
- **MongoDB Documentation:** https://docs.mongodb.com/
- **Apache Kafka Documentation:** https://kafka.apache.org/documentation/
- **JWT Specification:** https://tools.ietf.org/html/rfc7519
- **OpenAPI Specification:** https://spec.openapis.org/oas/latest.html

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tonyadji/langa/issues)
- **Email:** motodigo.appvenger@gmail.com

---

**Document Maintained By:** Development Team  
**Contact:** [Project Repository](https://github.com/tonyadji/langa)
