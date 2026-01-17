# Langa Backend - Explanation Documentation

**Document Type:** Explanation (Understanding-Oriented)  
**Audience:** All Technical Stakeholders  
**Purpose:** Understand design decisions, architecture, and core concepts

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Clean Architecture Implementation](#clean-architecture-implementation)
3. [Security Model](#security-model)
4. [Event-Driven Architecture](#event-driven-architecture)
5. [Multi-Tenancy Model](#multi-tenancy-model)
6. [Ingestion Pipeline](#ingestion-pipeline)
7. [Team Collaboration Model](#team-collaboration-model)
8. [Performance and Scalability](#performance-and-scalability)
9. [Design Decisions](#design-decisions)
10. [Trade-offs and Limitations](#trade-offs-and-limitations)

---

## Architecture Overview

### What is Langa Backend?

Langa Backend is a **centralized observability platform** that collects, stores, and provides access to logs and metrics from distributed applications. Think of it as a self-hosted alternative to commercial observability platforms like Datadog or New Relic, designed for teams who want full control over their monitoring data.

### Core Purpose

**Problem:** Distributed applications generate logs and metrics across multiple services, making it difficult to:
- Correlate events across services
- Troubleshoot issues
- Monitor system health
- Track performance trends

**Solution:** Langa provides:
- **Centralized Storage** - Single source of truth for all observability data
- **Multi-Tenant Support** - Multiple teams can use the same instance
- **Flexible Ingestion** - HTTP and Kafka protocols
- **Team Collaboration** - Share applications with colleagues
- **API-First Design** - Build custom dashboards and integrations

### System Context

```
┌─────────────────────────────────────────────────────────────┐
│                    Application Ecosystem                     │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  Java App     Python App     Node.js App     Go App         │
│     │             │               │              │           │
│     └─────────────┴───────────────┴──────────────┘           │
│                         │                                    │
│                         ▼                                    │
│                  ┌─────────────┐                            │
│                  │   Kafka     │                            │
│                  │   Broker    │                            │
│                  └──────┬──────┘                            │
│                         │                                    │
│                         ▼                                    │
│              ╔═════════════════════╗                        │
│              ║  Langa Backend      ║                        │
│              ║  ┌───────────────┐  ║                        │
│              ║  │ REST API      │  ║                        │
│              ║  ├───────────────┤  ║                        │
│              ║  │ Ingestion     │  ║                        │
│              ║  ├───────────────┤  ║                        │
│              ║  │ Domain Logic  │  ║                        │
│              ║  ├───────────────┤  ║                        │
│              ║  │ Event Bus     │  ║                        │
│              ║  └───────────────┘  ║                        │
│              ╚═════════════════════╝                        │
│                         │                                    │
│                         ▼                                    │
│                  ┌─────────────┐                            │
│                  │  MongoDB    │                            │
│                  └─────────────┘                            │
│                                                              │
│                         ▲                                    │
│                         │                                    │
│                  Dashboard Apps                              │
│              (React, Vue, Angular)                           │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Clean Architecture Implementation

### Why Clean Architecture?

Langa Backend follows **Clean Architecture** (also known as Hexagonal Architecture or Ports & Adapters) to achieve:

1. **Independence from Frameworks** - Business logic doesn't depend on Spring
2. **Testability** - Core logic can be tested without infrastructure
3. **Independence from UI** - Can add REST, GraphQL, gRPC without changing domain
4. **Independence from Database** - Can swap MongoDB for PostgreSQL
5. **Business Logic Isolation** - Domain rules are centralized and explicit

### Layer Structure

```
┌─────────────────────────────────────────────────────────────┐
│                     Presentation Layer                       │
│  (REST Controllers, Kafka Consumers, GraphQL Resolvers)      │
└────────────────────┬────────────────────────────────────────┘
                     │ Depends on ▼
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
│      (Use Cases, Event Listeners, Orchestration)             │
└────────────────────┬────────────────────────────────────────┘
                     │ Depends on ▼
┌─────────────────────────────────────────────────────────────┐
│                       Domain Layer                           │
│   (Entities, Value Objects, Domain Events, Repositories*)    │
│              * Interfaces only, no implementations           │
└─────────────────────────────────────────────────────────────┘
                     ▲ Implements
┌─────────────────────────────────────────────────────────────┐
│                   Infrastructure Layer                       │
│  (MongoDB Adapters, Kafka Config, Security, Email)          │
└─────────────────────────────────────────────────────────────┘
```

**Key Principle:** Dependencies flow inward. Domain layer has no dependencies.

### Domain Layer (`domain/`)

**Purpose:** Contains pure business logic with no framework dependencies.

**Components:**

1. **Entities** (e.g., `Application`, `User`, `Team`)
   - Core business objects with identity
   - Contain business rules and invariants
   - Example: `Application` enforces unique names per owner

2. **Value Objects** (e.g., `LogEntry`, `ShareWith`, `TeamMember`)
   - Immutable objects defined by their values
   - No identity
   - Example: `LogEntry` represents a single log event

3. **Domain Events** (e.g., `ApplicationSharedEvent`)
   - Represent things that happened in the domain
   - Trigger side effects (emails, notifications)

4. **Repository Interfaces**
   - Abstractions for persistence
   - No implementation details
   - Example: `ApplicationRepository` defines `save()`, `findById()`

5. **Use Case Interfaces**
   - Abstract business operations
   - Example: `CreateApplicationUseCase`

**Example Domain Entity:**

```java
@Getter
public class Application {
    private String id;
    private String name;
    private String key;
    private String accountKey;
    private String secret;
    private String owner;
    private Set<ShareWith> sharedWith;

    // Business rule: Names must be unique per owner
    public static Application createNew(String name, String accountKey, String owner) {
        Application app = new Application();
        app.name = name;
        app.key = generateKey();
        app.accountKey = accountKey;
        app.secret = generateSecret();
        app.owner = owner;
        app.sharedWith = new HashSet<>();
        return app;
    }

    // Business rule: Only owner can share
    public void shareWith(String accountOrTeamKey, SharedWithProfile profile) {
        if (isAlreadyShared(accountOrTeamKey)) {
            throw new AlreadySharedException();
        }
        sharedWith.add(new ShareWith(accountOrTeamKey, profile, LocalDateTime.now()));
    }

    // Business rule: Cannot share with self
    private boolean isAlreadyShared(String key) {
        return sharedWith.stream()
            .anyMatch(s -> s.getSharedWithKey().equals(key));
    }
}
```

**Benefits:**
- Business rules are explicit and testable
- No SQL, HTTP, or framework code
- Can be tested with plain JUnit (no Spring context)

### Application Layer (`application/`)

**Purpose:** Orchestrate domain objects to implement use cases.

**Components:**

1. **Use Cases** (e.g., `CreateApplicationUseCase`)
   - Implement specific business operations
   - Coordinate domain entities and repositories
   - Example: Create application, validate name, save, publish event

2. **Event Listeners**
   - React to domain events
   - Trigger side effects (send emails, update caches)
   - Example: `ApplicationSharedEventListener` sends email when app is shared

**Example Use Case:**

```java
@UseCase
public class CreateApplicationUseCase {
    private final ApplicationRepository repository;
    private final EventPublisher eventPublisher;

    public Application execute(String name, String accountKey, String owner) {
        // Business logic coordination
        if (repository.existsByOwnerAndName(owner, name)) {
            throw new ApplicationNameAlreadyExistsException();
        }

        Application app = Application.createNew(name, accountKey, owner);
        repository.save(app);
        
        // Publish event for side effects
        eventPublisher.publish(new ApplicationCreatedEvent(app.getId()));
        
        return app;
    }
}
```

**Why Separate from Domain?**
- Use cases are about "how" to do something
- Domain entities are about "what" the business rules are
- Use cases can change without affecting domain

### Infrastructure Layer (`infra/`)

**Purpose:** Implement technical details (database, HTTP, security).

**Components:**

1. **Adapters** (e.g., `MongoApplicationRepository`)
   - Implement repository interfaces from domain
   - Handle database-specific logic
   - Map between domain entities and database documents

2. **REST Controllers**
   - Handle HTTP requests/responses
   - Validate input
   - Call use cases
   - Transform domain objects to DTOs

3. **Security**
   - JWT authentication
   - Authorization filters
   - CORS configuration

4. **Kafka Consumers**
   - Listen to ingestion topic
   - Transform messages to domain events

**Example Repository Implementation:**

```java
@Repository
public class MongoApplicationRepository implements ApplicationRepository {
    private final MongoTemplate mongoTemplate;

    @Override
    public Application save(Application app) {
        ApplicationDocument doc = ApplicationMapper.toDocument(app);
        mongoTemplate.save(doc, "c_applications");
        return ApplicationMapper.toDomain(doc);
    }

    @Override
    public Optional<Application> findById(String id) {
        ApplicationDocument doc = mongoTemplate.findById(id, ApplicationDocument.class);
        return Optional.ofNullable(doc)
            .map(ApplicationMapper::toDomain);
    }
}
```

**Key Pattern:** Mapper classes convert between domain and infrastructure layers.

### Benefits of Clean Architecture

1. **Testability**
   - Domain layer: Pure unit tests
   - Application layer: Mock repositories
   - Infrastructure layer: Integration tests

2. **Flexibility**
   - Swap MongoDB for PostgreSQL: Only change infrastructure layer
   - Add GraphQL API: Only add new controllers
   - Change JWT to OAuth2: Only change security configuration

3. **Maintainability**
   - Business logic in one place (domain)
   - Clear separation of concerns
   - Easy to onboard new developers

4. **Evolutionary Architecture**
   - Can refactor one layer without affecting others
   - Can add features incrementally
   - Can replace components over time

---

## Security Model

### Overview

Langa Backend uses a **multi-layered security approach**:

1. **Authentication** - JWT tokens for API access
2. **Authorization** - Resource-level access control
3. **Ingestion Security** - HMAC signatures for data ingestion
4. **Transport Security** - HTTPS (TLS) for production

### JWT Authentication

**Why JWT?**
- **Stateless** - No server-side session storage required
- **Scalable** - Works across multiple backend instances
- **Decentralized** - Token contains all necessary information
- **Standard** - Widely supported by clients and libraries

**Token Structure:**

```
Header: { "alg": "HS256", "typ": "JWT" }
Payload: { 
  "sub": "user@example.com",
  "accountKey": "acc_abc123",
  "iat": 1704067200,
  "exp": 1704070800
}
Signature: HMACSHA256(header + payload, secret)
```

**Flow:**

```
1. User Login
   Client ─────[username, password]────> Backend
   Backend ────[validate credentials]───> Database
   Backend ────[generate tokens]────────> Client
   
   Response: {
     accessToken: "eyJhbGc...",  // 1 hour lifetime
     refreshToken: "8f7e6d..."   // 7 day lifetime
   }

2. API Request
   Client ─────[GET /applications]────> Backend
           [Authorization: Bearer eyJ...]
   Backend ────[verify signature]────> ✓ Valid
   Backend ────[extract user info]───> "user@example.com"
   Backend ────[execute request]─────> Response

3. Token Refresh
   Client ─────[POST /auth/refresh]──> Backend
           [refreshToken: "8f7e6d..."]
   Backend ────[validate token]──────> Database
   Backend ────[generate new tokens]─> Client
   Backend ────[invalidate old token]> Database
```

**Security Features:**
- **Expiration** - Access tokens expire after 1 hour
- **Refresh Rotation** - Refresh tokens are single-use
- **Signature Verification** - Prevents token tampering
- **HTTPS Only** - Tokens transmitted securely

### Authorization Model

**Principle:** Resource-based access control

**Application Access Levels:**

1. **Owner** - Full access
   - View secrets
   - Share/revoke access
   - View logs/metrics
   - Modify settings

2. **Shared User** - Read-only access
   - View logs/metrics
   - View basic info
   - Cannot view secrets
   - Cannot share

3. **Team Member** - Access through team sharing
   - Same as shared user
   - Access inherited from team

**Implementation:**

```java
public class Application {
    public boolean authorizedToAccess(String username, Set<String> accountKeys) {
        // Owner always has access
        if (owner.equals(username)) {
            return true;
        }
        
        // Check if shared with user's account
        boolean sharedWithUser = sharedWith.stream()
            .anyMatch(s -> s.getSharedWithProfile() == SharedWithProfile.USER
                && accountKeys.contains(s.getSharedWithKey()));
        
        // Check if shared with user's teams
        boolean sharedWithTeam = sharedWith.stream()
            .anyMatch(s -> s.getSharedWithProfile() == SharedWithProfile.TEAM
                && accountKeys.contains(s.getSharedWithKey()));
        
        return sharedWithUser || sharedWithTeam;
    }
}
```

**Team Authorization:**

```
Team Roles:
├── OWNER
│   ├── Create/delete team
│   ├── Invite/remove members (including admins)
│   ├── Change roles
│   └── Cannot be removed
│
├── ADMIN
│   ├── Invite members
│   ├── Remove members (except admins)
│   └── Cannot change roles
│
└── MEMBER
    └── View team info
```

### Ingestion Security (HMAC)

**Why HMAC Instead of JWT?**
- **Performance** - Faster than asymmetric crypto
- **Simplicity** - No token management needed
- **Replay Protection** - Timestamps and nonces prevent reuse
- **Dedicated Secrets** - Separate from API authentication

**Signature Calculation:**

```
Step 1: Build Payload
payload = userAgent + appKey + accountKey + timestamp + requestBody

Example:
payload = "java-client/1.0app_xyz123acc_abc7891704067200000{\"logs\":[...]}"

Step 2: Calculate HMAC
signature = Base64(HMAC-SHA256(secret, payload))

Step 3: Send Request
Headers:
  X-USER-AGENT: java-client/1.0
  X-APP-KEY: app_xyz123
  X-ACCOUNT-KEY: acc_abc789
  X-TIMESTAMP: 1704067200000
  X-AGENT-SIGNATURE: xY9zAb3... (base64 encoded)
Body:
  {"logs":[...]}
```

**Validation Process:**

```java
public class IngestionSecurity {
    public boolean validateSignature(HttpServletRequest request, String body) {
        // 1. Extract headers
        String userAgent = request.getHeader("X-USER-AGENT");
        String appKey = request.getHeader("X-APP-KEY");
        String accountKey = request.getHeader("X-ACCOUNT-KEY");
        String timestamp = request.getHeader("X-TIMESTAMP");
        String receivedSignature = request.getHeader("X-AGENT-SIGNATURE");
        
        // 2. Check timestamp (prevent old requests)
        if (isExpired(timestamp)) {
            throw new TimestampExpiredException();
        }
        
        // 3. Check nonce (prevent replay attacks)
        if (nonceRepository.exists(timestamp)) {
            throw new NonceAlreadyUsedException();
        }
        
        // 4. Get application secret
        String secret = applicationRepository.findSecretByKey(appKey);
        
        // 5. Calculate expected signature
        String payload = userAgent + appKey + accountKey + timestamp + body;
        String expectedSignature = calculateHmac(secret, payload);
        
        // 6. Compare signatures
        if (!MessageDigest.isEqual(
            receivedSignature.getBytes(),
            expectedSignature.getBytes()
        )) {
            throw new InvalidSignatureException();
        }
        
        // 7. Store nonce
        nonceRepository.save(timestamp);
        
        return true;
    }
}
```

**Security Properties:**
- **Authenticity** - Signature proves request came from app owner
- **Integrity** - Any modification invalidates signature
- **Replay Protection** - Timestamps expire, nonces prevent reuse
- **Confidentiality** - Secret never transmitted (only signature)

### CORS Configuration

**Purpose:** Allow web dashboards to call API from different origins.

**Configuration:**

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000", "https://dashboard.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**Why Needed?**
- Browsers enforce Same-Origin Policy
- Dashboard (http://localhost:3000) cannot call API (http://localhost:8080) without CORS
- CORS headers tell browser "this cross-origin request is allowed"

**Security Considerations:**
- **Restrict Origins** - Don't use `*` in production
- **Allow Credentials** - Needed for JWT tokens in cookies
- **Limit Methods** - Only allow necessary HTTP methods

---

## Event-Driven Architecture

### Why Event-Driven?

**Problems with Direct Coupling:**
1. Use cases become bloated (create app + send email + update cache)
2. Hard to test (mock email service in every test)
3. Difficult to add features (new side effect requires modifying use case)
4. Poor separation of concerns

**Event-Driven Solution:**
1. Use cases emit events (`ApplicationCreatedEvent`)
2. Listeners react to events independently
3. Easy to add new listeners without changing use cases
4. Side effects are decoupled

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       Use Case                               │
│  CreateApplicationUseCase                                    │
│  ├── Validate input                                          │
│  ├── Create domain entity                                    │
│  ├── Save to repository                                      │
│  └── Publish ApplicationCreatedEvent ──────┐                │
└────────────────────────────────────────────┼────────────────┘
                                              │
                     ┌────────────────────────┼────────────────┐
                     │         Event Bus      ▼                │
                     │  (Spring ApplicationEventPublisher)     │
                     └────────────────────────┬────────────────┘
                                              │
         ┌────────────────────────────────────┼────────────────┬─────────────────┐
         │                                    │                │                 │
         ▼                                    ▼                ▼                 ▼
┌────────────────────┐         ┌──────────────────┐  ┌────────────────┐  ┌────────────┐
│ Email Listener     │         │ Metrics Listener │  │ Cache Listener │  │ Future     │
│ ├── Send welcome   │         │ ├── Increment    │  │ ├── Invalidate │  │ Listeners  │
│ └── email          │         │ └── counter      │  │ └── cache      │  │            │
└────────────────────┘         └──────────────────┘  └────────────────┘  └────────────┘
```

### Outbox Pattern

**Problem:** What if event is published but transaction fails?

```
1. Save Application to Database  ✓
2. Publish ApplicationCreatedEvent ✓
3. Database Transaction Fails     ✗
   → Application not saved but event published!
   → Email sent for non-existent application
```

**Solution:** Outbox Pattern

```
Transaction:
1. Save Application to Database
2. Save ApplicationCreatedEvent to Outbox Table  ← Same transaction
3. Commit                                       ✓ or ✗ together

Background Process:
1. Read events from Outbox Table
2. Publish to Event Bus
3. Mark as published
4. Retry on failure
```

**Implementation:**

```java
@Service
public class OutboxEventPublisher {
    private final EventPublisher publisher;
    private final OutboxRepository outboxRepo;

    @Scheduled(fixedDelay = 1000) // Every second
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepo.findUnpublished();
        
        for (OutboxEvent event : events) {
            try {
                publisher.publish(event.getPayload());
                outboxRepo.markPublished(event.getId());
            } catch (Exception e) {
                // Will retry next cycle
                log.error("Failed to publish event", e);
            }
        }
    }
}
```

**Benefits:**
- **Reliable** - Events never lost
- **Consistent** - Domain changes and events are transactional
- **Resilient** - Retries on failure

### Event Listeners

**Example: Send Email When App is Shared**

```java
@Component
public class ApplicationSharedEventListener {
    private final EmailService emailService;
    private final UserRepository userRepository;

    @EventListener
    @Async
    public void handleApplicationShared(ApplicationSharedEvent event) {
        // Find who to notify
        String email = resolveEmail(event.getSharedWithKey(), event.getProfile());
        
        // Send notification
        emailService.send(
            email,
            "Application Shared",
            "You now have access to " + event.getApplicationName()
        );
    }
}
```

**Key Features:**
- `@EventListener` - Spring automatically calls this method
- `@Async` - Runs in background thread (doesn't block use case)
- **Decoupled** - Can be added/removed without changing use case

### Event Types in Langa

1. **ApplicationSharedEvent** - Application access granted
2. **FirstConnectionMailEvent** - User first login
3. **TeamInvitationSentEvent** - Team invitation created
4. **TeamInvitationAcceptedEvent** - Invitation accepted
5. **InvitationAcceptedMailEvent** - Notify host of acceptance

---

## Multi-Tenancy Model

### What is Multi-Tenancy?

**Definition:** Multiple independent "tenants" (users/organizations) share the same application instance.

**Example:**
- Company A uses Langa to monitor their apps
- Company B uses Langa to monitor their apps
- Both use the same Langa instance, but can't see each other's data

### Implementation Strategy

Langa uses **Shared Database, Discriminator Column** approach.

**All data is in the same MongoDB database, but filtered by tenant identifiers:**

1. **Account Key** - User-level isolation
2. **Application Key** - Application-level isolation
3. **Team Key** - Team-level isolation

### Data Isolation

**User Data:**
```javascript
// MongoDB Document
{
  "_id": "user_id_123",
  "email": "user@companyA.com",
  "accountKey": "acc_companyA_xyz",  // Tenant discriminator
  "password": "...",
  "role": "USER"
}
```

**Application Data:**
```javascript
{
  "_id": "app_id_456",
  "name": "Production API",
  "accountKey": "acc_companyA_xyz",  // Owner's account
  "owner": "user@companyA.com",
  "sharedWith": [
    {
      "sharedWithKey": "acc_companyA_abc",  // Another user from Company A
      "profile": "USER"
    },
    {
      "sharedWithKey": "team_companyA_123", // Company A's team
      "profile": "TEAM"
    }
  ]
}
```

**Log Data:**
```javascript
{
  "_id": "log_id_789",
  "appKey": "app_xyz",              // Which application
  "accountKey": "acc_companyA_xyz",  // Which tenant
  "message": "Error occurred",
  "level": "ERROR",
  "timestamp": "2025-12-28T10:00:00Z"
}
```

### Query Filtering

**All queries automatically filter by tenant:**

```java
public class LogEntryRepository {
    
    // User can only query logs from their accessible applications
    public Page<LogEntry> findLogs(String appKey, String userAccountKey, Pageable pageable) {
        // 1. Verify user has access to this application
        Application app = applicationRepository.findByKey(appKey);
        if (!app.authorizedToAccess(userAccountKey)) {
            throw new AccessDeniedException();
        }
        
        // 2. Query logs for this application only
        Query query = new Query()
            .addCriteria(Criteria.where("appKey").is(appKey))
            .with(pageable);
        
        return mongoTemplate.find(query, LogEntry.class);
    }
}
```

**Security Properties:**
- **Isolation** - Users can never see other tenants' data
- **Access Control** - Sharing is explicitly granted
- **Audit Trail** - All data tagged with tenant identifiers

### Sharing Model

**Three Levels of Sharing:**

1. **User-to-User Sharing**
   ```
   User A (acc_A) owns App X
   User A shares with User B (acc_B)
   → App X's sharedWith includes acc_B
   → User B can query logs for App X
   ```

2. **User-to-Team Sharing**
   ```
   User A owns App X
   User A shares with Team T (team_key_T)
   → App X's sharedWith includes team_key_T
   → All members of Team T can query logs for App X
   ```

3. **Team Membership**
   ```
   User B joins Team T
   → User B's account keys includes team_key_T
   → User B gains access to all apps shared with Team T
   ```

### Benefits

1. **Cost Efficiency** - Single infrastructure for all tenants
2. **Easy Deployment** - One instance to maintain
3. **Resource Sharing** - Database, Kafka, memory shared
4. **Scalability** - Add tenants without new infrastructure

### Considerations

1. **Noisy Neighbor** - One tenant's heavy usage affects others
   - Mitigation: Rate limiting, resource quotas
2. **Data Breach Risk** - Bug could expose other tenants' data
   - Mitigation: Thorough testing, security audits
3. **Compliance** - Some industries require physical isolation
   - Alternative: Deploy separate instances per customer

---

## Ingestion Pipeline

### Overview

The ingestion pipeline is the **heart of Langa** - it's how applications send logs and metrics.

### Two Ingestion Methods

#### 1. HTTP Ingestion (Synchronous)

**Use Case:** Low-volume applications, debugging, immediate feedback

**Flow:**

```
Application
    │
    │ HTTP POST /api/ingestion
    │ Headers: X-APP-KEY, X-AGENT-SIGNATURE, ...
    │ Body: {"logs": [...], "metrics": [...]}
    │
    ▼
┌─────────────────────────────────────┐
│  IngestionController (REST API)     │
│  ├── Validate signature             │
│  ├── Check timestamp                │
│  └── Forward to use case            │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  ProcessIngestionUseCase            │
│  ├── Parse logs/metrics             │
│  ├── Associate with application     │
│  └── Save to repository             │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  MongoDB                            │
│  ├── c_log_entries                  │
│  └── c_metric_entries               │
└─────────────────────────────────────┘
```

**Characteristics:**
- **Latency:** ~100-200ms
- **Throughput:** ~100 requests/second
- **Reliability:** HTTP retries
- **Feedback:** Immediate error response

#### 2. Kafka Ingestion (Asynchronous)

**Use Case:** High-volume applications, decoupled architecture

**Flow:**

```
Application
    │
    │ Kafka Producer
    │ Topic: langa-ingestion
    │ Message: {"logs": [...], "metrics": [...]}
    │
    ▼
┌─────────────────────────────────────┐
│  Kafka Cluster                      │
│  Topic: langa-ingestion             │
│  Partitions: 10                     │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  IngestionConsumer (Kafka Listener) │
│  ├── Consume messages               │
│  ├── Parse payload                  │
│  └── Forward to use case            │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  ProcessIngestionUseCase            │
│  (same as HTTP path)                │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  MongoDB                            │
└─────────────────────────────────────┘
```

**Characteristics:**
- **Latency:** Variable (depending on consumer lag)
- **Throughput:** ~10,000+ messages/second
- **Reliability:** Kafka durability, consumer retries
- **Feedback:** Async (no immediate confirmation)

### When to Use Each?

| Scenario | HTTP | Kafka |
|----------|------|-------|
| Low volume (<1000 logs/min) | ✓ | |
| High volume (>10,000 logs/min) | | ✓ |
| Need immediate feedback | ✓ | |
| Decoupled architecture | | ✓ |
| Simple setup | ✓ | |
| Fault tolerance | | ✓ |
| Network unreliable | | ✓ |

### Data Processing

**1. Parsing**

```java
public class ProcessIngestionUseCase {
    
    public void execute(IngestionRequest request) {
        // Parse logs
        List<LogEntry> logs = request.getLogs().stream()
            .map(dto -> LogEntry.builder()
                .message(dto.getMessage())
                .level(dto.getLevel())
                .loggerName(dto.getLoggerName())
                .timestamp(Instant.parse(dto.getTimestamp()))
                .threadName(dto.getThreadName())
                .stackTrace(dto.getStackTrace())
                .mdc(dto.getMdc())
                .build())
            .collect(Collectors.toList());
        
        // Parse metrics
        List<MetricEntry> metrics = request.getMetrics().stream()
            .map(this::parseMetric)
            .collect(Collectors.toList());
        
        // Associate with application
        logs.forEach(log -> log.setAppKey(request.getAppKey()));
        metrics.forEach(metric -> metric.setAppKey(request.getAppKey()));
        
        // Save
        logRepository.saveAll(logs);
        metricRepository.saveAll(metrics);
        
        // Update usage statistics
        updateUsage(request.getAppKey(), logs, metrics);
    }
}
```

**2. Validation**

- **Required Fields** - message, level, timestamp
- **Format Validation** - timestamp must be ISO-8601
- **Size Limits** - message max 1000 characters
- **Type Validation** - level must be valid log level

**3. Storage**

- **Batch Insert** - All logs/metrics saved in one operation
- **Indexing** - Indexed by appKey, timestamp, level
- **TTL** - Optional time-to-live for old data (not implemented)

**4. Usage Tracking**

```java
public void updateUsage(String appKey, List<LogEntry> logs, List<MetricEntry> metrics) {
    long logBytes = logs.stream()
        .mapToLong(LogEntry::getSizeInBytes)
        .sum();
    
    long metricBytes = metrics.stream()
        .mapToLong(MetricEntry::getSizeInBytes)
        .sum();
    
    usageRepository.incrementUsage(appKey, logBytes, metricBytes);
}
```

### Performance Optimizations

1. **Batching**
   - Applications batch 100 logs per request
   - Reduces HTTP overhead
   - Better MongoDB performance

2. **Async Processing**
   - Kafka consumers run in parallel
   - MongoDB inserts are async
   - Non-blocking I/O

3. **Indexing Strategy**
   ```javascript
   // Compound index for common queries
   db.c_log_entries.createIndex({ appKey: 1, timestamp: -1 })
   db.c_log_entries.createIndex({ accountKey: 1 })
   db.c_log_entries.createIndex({ level: 1 })
   ```

4. **Connection Pooling**
   - MongoDB connection pool: 100 connections
   - Reuse connections across requests

---

## Team Collaboration Model

### Purpose

**Problem:** Individual ownership doesn't scale.
- Large teams need shared access to applications
- Onboarding new engineers requires manual sharing
- Role changes require updating many applications

**Solution:** Team-based access control.

### Team Structure

```
Team: "Backend Engineers"
├── Owner: alice@example.com
│   └── Full control over team
│
├── Admin: bob@example.com
│   └── Can invite/remove members
│
├── Member: charlie@example.com
└── Member: diana@example.com
    └── Can access shared applications
```

### Workflow

**1. Create Team**

```
Alice creates "Backend Team"
→ Alice becomes OWNER
→ Team gets unique key: team_abc123
```

**2. Invite Members**

```
Alice invites Bob (ADMIN role)
→ Invitation created with expiration
→ Email sent to Bob
Bob accepts invitation
→ Bob added to team with ADMIN role
→ Alice receives confirmation email
```

**3. Share Application**

```
Alice shares "Production API" with "Backend Team"
→ Application's sharedWith includes team_abc123
→ All team members (Bob, Charlie, Diana) gain access
→ Future members automatically get access
```

**4. Access Control**

```
Charlie queries logs:
1. Charlie's account keys: [acc_charlie, team_abc123]
2. Application check: Is app shared with any of these keys?
   → Yes! Shared with team_abc123
3. Grant access
```

### Invitation Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│                   PENDING                                │
│  ├── Created by host                                     │
│  ├── Email sent to guest                                 │
│  └── Expiration timer started                            │
└────────┬────────────────────────┬────────────────────────┘
         │ Guest accepts          │ Time expires
         │                        │
         ▼                        ▼
┌─────────────────┐      ┌─────────────────┐
│   ACCEPTED      │      │    EXPIRED      │
│  ├── Guest added│      │  ├── Invitation │
│  │   to team    │      │  │   invalid    │
│  └── Host       │      │  └── Cannot     │
│      notified   │      │      accept     │
└─────────────────┘      └─────────────────┘
```

**Expiration Configuration:**
```properties
# Default: 7 days
team.invitation.expiration-days=7
```

### Role Hierarchy

```
             ┌─────────┐
             │  OWNER  │ ← Cannot be changed or removed
             └────┬────┘
                  │ Can manage
                  ▼
             ┌─────────┐
             │  ADMIN  │ ← Can invite/remove members
             └────┬────┘
                  │ Can invite
                  ▼
             ┌─────────┐
             │ MEMBER  │ ← Can view team info
             └─────────┘
```

**Permission Matrix:**

| Action | OWNER | ADMIN | MEMBER |
|--------|-------|-------|--------|
| Invite members | ✓ | ✓ | ✗ |
| Remove members | ✓ | ✓ (except admins) | ✗ |
| Change roles | ✓ | ✗ | ✗ |
| Delete team | ✓ | ✗ | ✗ |
| View team | ✓ | ✓ | ✓ |

### Benefits

1. **Scalability** - Share once with team instead of each person
2. **Automation** - New members automatically get access
3. **Organization** - Mirror company structure
4. **Governance** - Clear ownership and roles

### Use Cases

1. **Engineering Teams**
   ```
   "Backend Team" → Access to backend services
   "Frontend Team" → Access to web applications
   "DevOps Team" → Access to infrastructure logs
   ```

2. **Project-Based**
   ```
   "Project Apollo" → All applications for this project
   "Project Beta" → All beta testing applications
   ```

3. **Customer-Facing**
   ```
   "Customer A Support" → Access to Customer A's applications
   "Customer B Support" → Access to Customer B's applications
   ```

---

## Performance and Scalability

### Current Performance

**Measured on standard deployment (4 CPU, 8GB RAM):**

| Metric | Value |
|--------|-------|
| HTTP Ingestion | ~200 req/sec |
| Kafka Ingestion | ~10,000 msg/sec |
| Log Query | <2 seconds (100K logs) |
| Concurrent Users | ~100 |
| Database Size | ~1GB per 1M logs |

### Bottlenecks

1. **Database Writes**
   - MongoDB write throughput limited
   - Solution: Sharding, replica sets

2. **HTTP Request Handling**
   - Tomcat thread pool limited (200 threads)
   - Solution: Increase threads, use WebFlux (reactive)

3. **Query Performance**
   - Full table scans on unindexed fields
   - Solution: Add indexes, use aggregation pipeline

### Scaling Strategies

#### Vertical Scaling (Scale Up)

**Increase resources on single instance:**

```yaml
# Kubernetes example
resources:
  requests:
    memory: "4Gi"
    cpu: "2"
  limits:
    memory: "8Gi"
    cpu: "4"
```

**Pros:**
- Simple (no code changes)
- No distributed system complexity

**Cons:**
- Limited by hardware
- Single point of failure
- Expensive

#### Horizontal Scaling (Scale Out)

**Run multiple instances behind load balancer:**

```
           ┌──────────────┐
           │ Load Balancer│
           └──────┬───────┘
                  │
      ┌───────────┼───────────┐
      │           │           │
      ▼           ▼           ▼
┌──────────┐ ┌──────────┐ ┌──────────┐
│Instance 1│ │Instance 2│ │Instance 3│
└────┬─────┘ └────┬─────┘ └────┬─────┘
     │            │            │
     └────────────┼────────────┘
                  ▼
           ┌──────────────┐
           │   MongoDB    │
           │ Replica Set  │
           └──────────────┘
```

**Requirements for Horizontal Scaling:**

1. **Stateless Application**
   - ✓ JWT tokens (no server-side sessions)
   - ✓ No in-memory caching
   - ✓ All state in MongoDB

2. **Shared Database**
   - ✓ MongoDB shared across instances
   - ✓ Replica set for high availability

3. **Load Balancer**
   - Round-robin distribution
   - Health checks
   - Session affinity (not needed for stateless)

**Deployment:**

```bash
# Kubernetes
kubectl scale deployment langa-backend --replicas=5

# Docker Compose
docker-compose up --scale langa-backend=5
```

**Auto-Scaling (Kubernetes HPA):**

```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: langa-backend-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: langa-backend
  minReplicas: 2
  maxReplicas: 20
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
```

### Database Scaling

#### MongoDB Replica Sets

**Purpose:** High availability and read scaling

```
┌─────────────────────────────────────────────────────────┐
│                   Replica Set                            │
│                                                          │
│  ┌──────────┐      ┌──────────┐      ┌──────────┐      │
│  │ PRIMARY  │─────>│SECONDARY │      │SECONDARY │      │
│  │  (Write) │      │  (Read)  │      │  (Read)  │      │
│  └──────────┘      └──────────┘      └──────────┘      │
│       │                                                  │
│       └──> Replicates to secondaries                    │
└─────────────────────────────────────────────────────────┘
```

**Benefits:**
- Automatic failover
- Read distribution
- Zero-downtime backups

**Configuration:**

```javascript
// MongoDB shell
rs.initiate({
  _id: "langa-replica-set",
  members: [
    { _id: 0, host: "mongo-1:27017" },
    { _id: 1, host: "mongo-2:27017" },
    { _id: 2, host: "mongo-3:27017" }
  ]
})
```

#### MongoDB Sharding

**Purpose:** Horizontal data partitioning

```
Application
     │
     ▼
┌─────────────┐
│   mongos    │ ← Query router
└──────┬──────┘
       │
   ┌───┴───┬────────┬────────┐
   ▼       ▼        ▼        ▼
Shard 1  Shard 2  Shard 3  Shard 4
appKey   appKey   appKey   appKey
0-25%    25-50%   50-75%   75-100%
```

**Shard Key:** `appKey` (even distribution across applications)

**Benefits:**
- Unlimited storage growth
- Distributed writes
- Parallel queries

**When to Shard:**
- >1TB data
- >100GB per application
- Write throughput exceeds single server

### Caching Strategy

**Current:** No caching (all queries hit database)

**Potential Improvements:**

1. **Application-Level Cache (Redis)**
   ```
   GET /applications
   → Check Redis cache
   → If miss, query MongoDB
   → Store in Redis (TTL: 5 minutes)
   → Return result
   ```

2. **Query Result Cache**
   ```
   GET /applications/{id}/logs?level=ERROR&page=0
   → Hash query parameters
   → Check cache
   → If miss, query MongoDB and cache
   ```

3. **Aggregation Cache**
   ```
   GET /applications/{id}/metrics/summary
   → Pre-computed aggregations stored in Redis
   → Updated periodically
   ```

### Future Optimizations

1. **Read Replicas** - Separate read and write traffic
2. **Time-Series Optimization** - Use MongoDB time-series collections
3. **Data Archival** - Move old logs to cold storage (S3)
4. **Compression** - Compress log/metric payloads
5. **Connection Pooling** - Tune MongoDB connection pool
6. **Async Processing** - Use reactive streams (WebFlux)

---

## Design Decisions

### Why MongoDB Instead of SQL?

**Reasons:**

1. **Schema Flexibility**
   - Log entries have variable fields (MDC)
   - No need for rigid schema
   - Easy to add new fields

2. **Write Performance**
   - Optimized for high-throughput writes
   - Log ingestion is write-heavy

3. **Horizontal Scaling**
   - Native sharding support
   - Easier to scale than SQL

4. **JSON Native**
   - REST API uses JSON
   - No ORM impedance mismatch

**Trade-offs:**

❌ **No Joins** - Have to denormalize data or multiple queries
❌ **Eventual Consistency** - Replica set reads may be stale
❌ **No Transactions** - Limited multi-document transactions (though supported in newer versions)

### Why JWT Instead of Sessions?

**Reasons:**

1. **Stateless** - No server-side storage
2. **Scalable** - Works across multiple instances
3. **Mobile-Friendly** - Easy to store on mobile devices
4. **Standard** - Widely supported

**Trade-offs:**

❌ **Cannot Revoke** - Tokens valid until expiration
   - Mitigation: Short expiration (1 hour)
❌ **Size** - Larger than session ID
   - Mitigation: Keep payload small

### Why Event-Driven Architecture?

**Reasons:**

1. **Decoupling** - Side effects don't pollute use cases
2. **Extensibility** - Easy to add new listeners
3. **Testability** - Test use cases without side effects
4. **Asynchronous** - Don't block on email sending

**Trade-offs:**

❌ **Complexity** - More moving parts
❌ **Debugging** - Harder to trace flow
❌ **Eventual Consistency** - Events processed asynchronously

### Why Clean Architecture?

**Reasons:**

1. **Testability** - Domain logic fully tested
2. **Flexibility** - Can swap infrastructure
3. **Maintainability** - Clear separation of concerns
4. **Evolutionary** - Can refactor incrementally

**Trade-offs:**

❌ **Boilerplate** - More classes and interfaces
❌ **Learning Curve** - Developers must understand pattern
❌ **Over-Engineering** - Simple apps don't need it

---

## Trade-offs and Limitations

### Known Limitations

1. **No Delete Operations**
   - Applications cannot be deleted
   - Users cannot be deleted
   - **Reason:** Preserves audit trail, simplifies implementation
   - **Workaround:** Mark as inactive/archived

2. **Basic Query Capabilities**
   - No full-text search
   - No aggregations (sum, avg, etc.)
   - No complex filters (regex, wildcards)
   - **Reason:** MVP implementation, focus on core features
   - **Future:** Add Elasticsearch integration

3. **No Real-Time Updates**
   - Dashboards must poll for new logs
   - No WebSocket/SSE support
   - **Reason:** Stateless architecture, complexity
   - **Future:** Add WebSocket support

4. **Limited Ingestion Validation**
   - No schema enforcement
   - Minimal field validation
   - **Reason:** Performance, flexibility
   - **Risk:** Bad data can be ingested

5. **No Multi-Region Support**
   - Single MongoDB cluster
   - No geo-distribution
   - **Reason:** Complexity, cost
   - **Future:** Multi-region replication

### Design Trade-offs

#### Clean Architecture vs. Simplicity

**Chose:** Clean Architecture

**Benefits:**
- Testability
- Maintainability
- Flexibility

**Cost:**
- More code
- Steeper learning curve
- Can feel over-engineered for simple features

**When to Reconsider:**
- Small team (<5 developers)
- Simple domain (CRUD only)
- Short-term project

#### JWT vs. Session Cookies

**Chose:** JWT

**Benefits:**
- Stateless (scales horizontally)
- Works across domains
- Mobile-friendly

**Cost:**
- Cannot revoke before expiration
- Larger payload
- Requires careful secret management

**When to Reconsider:**
- Need instant revocation (e.g., logout)
- Highly sensitive data
- Single-domain application

#### MongoDB vs. PostgreSQL

**Chose:** MongoDB

**Benefits:**
- Schema flexibility
- Write performance
- JSON native

**Cost:**
- No relational integrity
- Limited complex queries
- Eventual consistency

**When to Reconsider:**
- Strong relational needs
- Complex reporting queries
- Transactional guarantees critical

#### HTTP + Kafka vs. gRPC

**Chose:** HTTP + Kafka

**Benefits:**
- Simple clients
- Wide compatibility
- Kafka for high throughput

**Cost:**
- HTTP not as efficient as gRPC
- Two ingestion paths to maintain

**When to Reconsider:**
- Internal services only (gRPC overhead acceptable)
- Need bi-directional streaming
- Performance critical (gRPC ~30% faster)

---

## Conclusion

Langa Backend is designed with the following principles:

1. **Pragmatic Architecture** - Clean Architecture where it adds value
2. **Scalability** - Horizontally scalable, stateless design
3. **Security-First** - Multi-layered security (JWT, HMAC, authorization)
4. **Developer Experience** - Easy to integrate, clear API
5. **Extensibility** - Event-driven, plugin-friendly

**Best Suited For:**
- Teams wanting self-hosted observability
- Multi-tenant SaaS observability products
- Learning platform for backend architecture
- Base for custom monitoring solutions

**Not Ideal For:**
- Extremely high scale (>1M logs/second) - consider specialized solutions
- Real-time dashboards - lacks WebSocket support
- Compliance requiring deletion - no hard deletes
- Complex analytics - limited query capabilities

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tonyadji/langa/issues)
- **Email:** motodigo.appvenger@gmail.com

---

**For More Information:**
- [Specification](./01-SPECIFICATION.md) - Complete technical spec
- [Tutorial](./02-TUTORIAL.md) - Hands-on getting started
- [How-To Guides](./03-HOW-TO.md) - Specific task recipes
- [Reference](./04-REFERENCE.md) - API and configuration reference

---

**Document Version:** 1.0  
**Last Updated:** December 28, 2025  
**Maintainers:** Langa Development Team
