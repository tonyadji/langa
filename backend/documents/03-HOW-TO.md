# Langa Backend - How-To Guides

**Document Type:** How-To Guides (Task-Oriented)  
**Audience:** Developers, DevOps Engineers  
**Purpose:** Step-by-step recipes for specific tasks

---

## Table of Contents

1. [Application Management](#application-management)
2. [Ingestion Implementation](#ingestion-implementation)
3. [Team Collaboration](#team-collaboration)
4. [Querying and Filtering](#querying-and-filtering)
5. [Security and Authentication](#security-and-authentication)
6. [Deployment](#deployment)
7. [Monitoring and Maintenance](#monitoring-and-maintenance)

---

## Application Management

### How to Create an Application

**Prerequisites:** Authenticated user with a valid access token (see [Security and Authentication](#security-and-authentication))

**Steps:**

1. **Make the API call:**
```bash
curl -X POST http://localhost:8080/api/applications \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "Production API"
  }'
```

2. **Save the response:**
```json
{
  "id": "app_id",
  "name": "Production API",
  "key": "app_xyz123",
  "accountKey": "acc_abc789",
  "owner": "you@example.com",
  "sharedWith": []
}
```

3. **Get ingestion credentials:**
```bash
curl -X GET http://localhost:8080/api/applications/app_id/secured-details \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Key Points:**
- Application names must be unique per user
- Keys are auto-generated
- Store the secret securely - it's only returned once

---

### How to List All Accessible Applications

**Use Case:** View applications you own or have shared access to

```bash
curl -X GET http://localhost:8080/api/applications \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response Structure:**
```json
[
  {
    "id": "owned_app_id",
    "name": "My App",
    "key": "app_key",
    "accountKey": "my_account",
    "owner": "you@example.com",
    "sharedWith": [...]
  },
  {
    "id": "shared_app_id",
    "name": "Team App",
    "owner": "teammate@example.com",
    "sharedWith": [...]
    // Note: No keys for shared apps
  }
]
```

**Filtering:**
- Owned apps include full details with keys
- Shared apps exclude sensitive fields

---

### How to Share an Application with a User

**Prerequisites:** You must be the application owner

**Steps:**

1. **Get the user's account key** (they provide this from their profile)

2. **Share the application:**
```bash
curl -X POST http://localhost:8080/api/applications/APP_ID/share \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "sharedWith": "colleague@example.com",
    "profile": "USER"
  }'
```

3. **Verify sharing:**
```bash
curl -X GET http://localhost:8080/api/applications/APP_ID \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Check the `sharedWith` array for the new entry.

**What They Can Do:**
- ✅ View logs and metrics
- ✅ View basic application info
- ❌ View secrets or keys
- ❌ Modify the application
- ❌ Share with others

---

### How to Share an Application with a Team

**Use Case:** Grant access to all team members at once

**Steps:**

1. **Create a team first** (see Team Collaboration section)

2. **Get the team key:**
```bash
curl -X GET http://localhost:8080/api/teams \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

3. **Share with the team:**
```bash
curl -X POST http://localhost:8080/api/applications/APP_ID/share \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "sharedWith": "team_key_xyz",
    "profile": "TEAM"
  }'
```

**Benefits:**
- All current and future team members get access
- Single sharing action instead of individual shares
- Easier access management

---

### How to Revoke Application Access

**Steps:**

```bash
curl -X POST http://localhost:8080/api/applications/APP_ID/revoke \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "sharedWith": "user@example.com",
    "profile": "USER"
  }'
```

**For Teams:**
```bash
curl -X POST http://localhost:8080/api/applications/APP_ID/revoke \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "sharedWith": "team_key_xyz",
    "profile": "TEAM"
  }'
```

**Notes:**
- Revocation is immediate
- Users lose access to all logs and metrics
- They won't be notified automatically

---

### How to Check Application Usage

**Use Case:** Monitor storage consumption

```bash
curl -X GET http://localhost:8080/api/applications/APP_ID/usage \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response:**
```json
{
  "id": "APP_ID",
  "key": "app_xyz",
  "name": "My App",
  "logUsage": 52428800,
  "metricUsage": 10485760
}
```

**Units:** Bytes

**Calculations:**
- `logUsage`: Sum of all log entry sizes
- `metricUsage`: Sum of all metric entry sizes

**Typical Sizes:**
- Simple log entry: ~200-500 bytes
- Log with stack trace: ~2-5 KB
- Metric entry: ~150-250 bytes

---

## Ingestion Implementation

### How to Send Logs from a Java Application

**Use Case:** Send logs and method metrics from any Java 17+ application (Spring Boot or not)

Use the **Langa agent** rather than writing a client: it handles batching, signing, retries and back-pressure.

**Step 1: Get the Agent**

Download `langa-agent` from [Maven Central](https://central.sonatype.com/artifact/com.capricedumardi/langa-agent),
or add it as a dependency to use `@Monitored`.

**Step 2: Configure It**

```bash
export LOGGING_FRAMEWORK=logback                                  # or log4j2
export LANGA_INGESTION_URL=<ingestion URL shown in the dashboard>
export LANGA_INGESTION_SECRET=<application secret>
```

**Step 3: Start Your Application with the Agent**

```bash
java -javaagent:langa-agent.jar -jar your-application.jar
```

Every log event goes to Langa. Annotate methods with `@Monitored` to record their execution time.

**Details:** [agent README](../../agent/README.md) and
[configuration guide](../../agent/src/main/resources/configuration-guide.md).

---

### How to Implement HTTP Ingestion in Python

**Use Case:** Send logs from a Python application

**Step 1: Install Dependencies**

```bash
pip install requests
```

**Step 2: Create Ingestion Client**

```python
import hashlib
import hmac
import base64
import json
import time
import requests
import secrets
from typing import List, Dict, Optional

class LangaClient:
    def __init__(self, app_key: str, account_key: str, secret: str, ingestion_url: str,
                 agent_version: str = "langa-agent-v1.0.0"):
        self.app_key = app_key
        self.account_key = account_key
        self.secret = secret
        self.ingestion_url = ingestion_url
        self.user_agent = agent_version  # must equal AGENT_VERSION on the backend

    def send_logs(self, logs: List[Dict]) -> None:
        timestamp = str(int(time.time() * 1000))
        nonce = secrets.token_hex(16)  # single use
        body = json.dumps({
            "type": "LOG",
            "appKey": self.app_key,
            "accountKey": self.account_key,
            "entries": logs,
        })

        headers = {
            "X-USER-AGENT": self.user_agent,
            "X-APP-KEY": self.app_key,
            "X-ACCOUNT-KEY": self.account_key,
            "X-TIMESTAMP": timestamp,
            "X-AGENT-SIGNATURE": self._calculate_signature(timestamp, nonce),
            "Content-Type": "application/json"
        }

        response = requests.post(self.ingestion_url, data=body, headers=headers)
        response.raise_for_status()

    def _calculate_signature(self, timestamp: str, nonce: str) -> str:
        message = f"{self.app_key}{self.account_key}{self.user_agent}{timestamp}{nonce}HTTP"
        digest = hmac.new(
            self.secret.encode('utf-8'),
            message.encode('utf-8'),
            hashlib.sha256
        ).digest()
        return f"{nonce}:{base64.b64encode(digest).decode('utf-8')}"
```

**Step 3: Use the Client**

```python
from datetime import datetime, timezone
import traceback

# Initialize client
langa = LangaClient(
    app_key="app_xyz123",
    account_key="acc_abc789",
    secret="sec_your_secret",
    ingestion_url="http://localhost:8080/api/ingestion"
)

# Send logs
try:
    # Your application logic
    result = do_something()
except Exception as e:
    langa.send_logs([{
        "message": str(e),
        "level": "ERROR",
        "loggerName": __name__,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "threadName": "main",
        "stackTrace": traceback.format_exc(),
        "mdc": {}
    }])
```

**Step 4: Create a Logging Handler (Advanced)**

```python
import logging

class LangaHandler(logging.Handler):
    def __init__(self, langa_client: LangaClient):
        super().__init__()
        self.client = langa_client
        self.buffer = []
        self.buffer_size = 10
    
    def emit(self, record: logging.LogRecord):
        log_entry = {
            "message": self.format(record),
            "level": record.levelname,
            "loggerName": record.name,
            "timestamp": datetime.fromtimestamp(record.created, timezone.utc).isoformat(),
            "threadName": record.threadName,
            "stackTrace": self.format_exception(record.exc_info) if record.exc_info else None,
            "mdc": {}
        }
        
        self.buffer.append(log_entry)
        
        if len(self.buffer) >= self.buffer_size:
            self.flush()
    
    def flush(self):
        if self.buffer:
            try:
                self.client.send_logs(self.buffer)
                self.buffer = []
            except Exception:
                # Handle ingestion failure
                pass
    
    @staticmethod
    def format_exception(exc_info):
        import traceback
        return ''.join(traceback.format_exception(*exc_info))

# Usage
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)
logger.addHandler(LangaHandler(langa))

logger.info("Application started")  # Automatically sent to Langa
```

---

### How to Implement Kafka Ingestion

**Use Case:** High-throughput asynchronous ingestion

**Prerequisites:**
- Kafka cluster reachable by your applications and by the backend
- Backend `KAFKA_*` variables set (see [reference](04-REFERENCE.md#configuration-reference))

**With the agent (recommended)**

The agent picks its sender from the ingestion URL of the application: use the Kafka ingestion URL shown in
the dashboard as `LANGA_INGESTION_URL` and it publishes batches asynchronously (compression, retries,
circuit breaker). Tune it with the `langa.kafka.*` settings of the
[configuration guide](../../agent/src/main/resources/configuration-guide.md).

**Message format (custom producers)**

- Topic: `langa` (the consumer currently listens on this fixed topic)
- Value: the same JSON body as HTTP ingestion — `type` (`LOG` or `METRIC`), `appKey`, `accountKey`, `entries`
- Headers: `xUserAgent`, `xAppKey`, `xAccountKey`, `xTimestamp`, `xNonce`, `xAgentSignature`
- Signature: same HMAC as HTTP, with credential type `KAFKA` instead of `HTTP`
  (see [specification §4.3](01-SPECIFICATION.md#43-ingestion-security))

**Benefits:**
- Asynchronous (non-blocking)
- High throughput
- Decoupled from Langa Backend availability

**Considerations:**
- Requires Kafka infrastructure
- No immediate confirmation of ingestion
- More complex setup than HTTP

---

### How to Batch Logs for Better Performance

**Problem:** Sending logs individually is inefficient

**Solution:** Buffer and batch logs

> The Langa agent already does this (`langa.buffer.*` settings). The sketch below applies to custom clients.

**Implementation (Java):**

```java
@Component
public class BatchingLangaClient {
    
    private final LangaIngestionClient client;
    private final List<LogDto> buffer = new CopyOnWriteArrayList<>();
    private final int batchSize;
    private final ScheduledExecutorService scheduler;
    
    public BatchingLangaClient(LangaIngestionClient client,
                               @Value("${langa.batch-size:100}") int batchSize,
                               @Value("${langa.flush-interval-seconds:10}") int flushInterval) {
        this.client = client;
        this.batchSize = batchSize;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Flush periodically
        scheduler.scheduleAtFixedRate(
            this::flush, 
            flushInterval, 
            flushInterval, 
            TimeUnit.SECONDS
        );
    }
    
    public void addLog(LogDto log) {
        buffer.add(log);
        
        if (buffer.size() >= batchSize) {
            flush();
        }
    }
    
    public synchronized void flush() {
        if (buffer.isEmpty()) {
            return;
        }
        
        List<LogDto> toSend = new ArrayList<>(buffer);
        buffer.clear();
        
        try {
            client.sendLogs(toSend);
        } catch (Exception e) {
            // Handle failure - maybe re-add to buffer or dead letter queue
            buffer.addAll(toSend);
        }
    }
    
    @PreDestroy
    public void shutdown() {
        flush();
        scheduler.shutdown();
    }
}
```

**Configuration:**

```properties
langa.batch-size=100
langa.flush-interval-seconds=10
```

**Benefits:**
- Reduces HTTP requests by 100x
- Lower network overhead
- Better throughput
- Still maintains reasonable latency (10 second window)

---

## Team Collaboration

### How to Create a Team

**Steps:**

```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "Backend Team"
  }'
```

**Response:**
```json
{
  "id": "team_id_123",
  "name": "Backend Team",
  "key": "team_key_xyz",
  "role": "OWNER"
}
```

**Notes:**
- You automatically become the OWNER
- Team names are not enforced unique
- Save the `key` for sharing applications with the team

---

### How to Invite Someone to a Team

**Prerequisites:** You must be OWNER or ADMIN

**Steps:**

```bash
curl -X POST http://localhost:8080/api/teams/invite \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "teamKey": "team_key_xyz",
    "guestEmail": "newmember@example.com",
    "role": "MEMBER"
  }'
```

**Role Options:**
- `MEMBER` - Standard access
- `ADMIN` - Can manage members
- `OWNER` - Cannot be assigned (only creator)

**What Happens:**
1. Invitation is created and stored
2. Email is sent to the guest (if mail configured)
3. Invitation expires after configured duration

---

### How to Accept a Team Invitation

**As the Invited User:**

**Step 1: List Your Invitations**

```bash
curl -X GET http://localhost:8080/api/team-invitations \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response:**
```json
[
  {
    "id": "invitation_id_123",
    "teamKey": "team_key_xyz",
    "teamName": "Backend Team",
    "hostEmail": "teamlead@example.com",
    "role": "MEMBER",
    "expirationDate": "2026-01-28T10:00:00"
  }
]
```

**Step 2: Accept the Invitation**

```bash
curl -X POST http://localhost:8080/api/team-invitations/accept \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "invitationId": "invitation_id_123"
  }'
```

**Result:**
- You're now a member of the team
- You can access applications shared with the team
- Host is notified via email

---

### How to Manage Team Members

**View Team Members:**

```bash
curl -X GET http://localhost:8080/api/teams/team_key_xyz/members \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Change Member Role (OWNER or ADMIN only):**

```bash
curl -X PUT http://localhost:8080/api/teams/team_key_xyz/members \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "email": "member@example.com",
    "role": "ADMIN"
  }'
```

**Remove Member (OWNER only for ADMIN, OWNER/ADMIN for MEMBER):**

```bash
curl -X DELETE http://localhost:8080/api/teams/team_key_xyz/members/member@example.com \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Restrictions:**
- OWNER cannot be removed
- OWNER cannot change their role
- Only OWNER can remove ADMIN

---

## Querying and Filtering

### How to Filter Logs by Level

**Use Case:** Show only errors

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/logs?level=ERROR" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Supported Levels:**
- `TRACE`
- `DEBUG`
- `INFO`
- `WARN`
- `ERROR`
- `FATAL`

---

### How to Filter Logs by Logger Name

**Use Case:** Focus on specific package or class

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/logs?loggerName=com.example.UserService" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Pattern Matching:**
- Exact match: `com.example.UserService`
- Prefix match (if supported): `com.example.*`

---

### How to Query Logs by Time Range

**Use Case:** Show logs from the last hour

```bash
START_TIME=$(date -u -v-1H +%Y-%m-%dT%H:%M:%SZ)
END_TIME=$(date -u +%Y-%m-%dT%H:%M:%SZ)

curl -X GET "http://localhost:8080/api/applications/APP_ID/logs?startTime=${START_TIME}&endTime=${END_TIME}" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Format:** ISO-8601 with timezone (e.g., `2025-12-28T10:00:00Z`)

**Examples:**
- Last 24 hours: `startTime=2025-12-27T10:00:00Z`
- Specific incident window: `startTime=2025-12-28T14:00:00Z&endTime=2025-12-28T14:30:00Z`

---

### How to Paginate Results

**Use Case:** Handle large result sets

```bash
# Get first page (100 items)
curl -X GET "http://localhost:8080/api/applications/APP_ID/logs?page=0&size=100" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"

# Get second page
curl -X GET "http://localhost:8080/api/applications/APP_ID/logs?page=1&size=100" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response Structure:**
```json
{
  "content": [...],
  "totalElements": 5432,
  "currentPage": 0,
  "pageSize": 100
}
```

**Calculations:**
- Total pages: `Math.ceil(totalElements / pageSize)`
- Has next: `currentPage < totalPages - 1`

**Default Values:**
- `page`: 0
- `size`: 100

---

### How to Combine Multiple Filters

**Use Case:** Query ERROR logs from specific logger in time range

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/logs?level=ERROR&loggerName=com.example.PaymentService&startTime=2025-12-28T00:00:00Z&page=0&size=50" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**All Filters:**
- `level` - Log level
- `loggerName` - Logger name
- `startTime` - Start of time range
- `endTime` - End of time range
- `page` - Page number
- `size` - Page size

---

### How to Query Metrics

**Basic Query:**

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/metrics" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Filter by Metric Name:**

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/metrics?name=http.request" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Filter by Status:**

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/metrics?status=SUCCESS" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Time Range and Pagination:**

```bash
curl -X GET "http://localhost:8080/api/applications/APP_ID/metrics?startTime=2025-12-28T00:00:00Z&endTime=2025-12-28T23:59:59Z&page=0&size=100" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Security and Authentication

### How to Get an Access Token for API Calls

**Problem:** You want to call the API with curl or Postman, without the dashboard

**Solution:** enable the development token endpoint (never in production) and use an e-mail one-time code:

```bash
curl -X POST http://localhost:8080/api/dev/token/start -H "Content-Type: application/json" \
     -d '{"username":"user@example.com"}'
curl -X POST http://localhost:8080/api/dev/token/complete -H "Content-Type: application/json" \
     -d '{"continuationToken":"…","code":"12345678"}'
```

Setup and error codes: [docs/authentication.md](../../docs/authentication.md#getting-a-token-without-the-dashboard-dev-only).

---

### How to Handle an Expired Token

**Problem:** API answers `401 Unauthorized`

**Solution:** access tokens are issued by the identity provider and expire after about an hour. The backend
has no refresh endpoint:
- **Dashboard** - MSAL renews tokens silently (`acquireTokenSilent`) and falls back to an interactive sign-in
- **Scripts** - request a new token with the dev-token endpoint

---

### How to Handle Authentication in a Dashboard

**Solution:** sign users in with the identity provider using authorization code + PKCE, then send the access
token on each call. The Langa dashboard does it with MSAL behind an `AuthClient` interface:

```typescript
// Axios interceptor: attach a fresh token to every API call
api.interceptors.request.use(async (config) => {
  const token = await authClient.getAccessToken();   // MSAL acquireTokenSilent under the hood
  config.headers.Authorization = `Bearer ${token}`;
  return config;
});
```

See `frontend/src/features/auth` and [docs/authentication.md](../../docs/authentication.md) for the app
registrations and variables.

---

### How to Switch Identity Provider

**Solution:** set the `AUTH_*` variables for the new provider (a Cognito example is given in
[docs/authentication.md](../../docs/authentication.md#backend-settings)) and implement the matching
`AuthClient` in the dashboard. Existing users are linked by e-mail on their first sign-in.

---

### How to Secure Ingestion Credentials

**Problem:** Secrets should not be hardcoded

**Solutions:**

**1. Environment Variables**

```bash
# .env file (never commit!)
LANGA_APP_KEY=app_xyz123
LANGA_ACCOUNT_KEY=acc_abc789
LANGA_SECRET=sec_your_secret
```

```java
@Value("${LANGA_APP_KEY}")
private String appKey;
```

**2. AWS Secrets Manager**

```java
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class SecretManager {
    
    public static String getSecret(String secretName) {
        SecretsManagerClient client = SecretsManagerClient.create();
        GetSecretValueRequest request = GetSecretValueRequest.builder()
            .secretId(secretName)
            .build();
        return client.getSecretValue(request).secretString();
    }
}

// Usage
String secret = SecretManager.getSecret("langa-ingestion-secret");
```

**3. HashiCorp Vault**

```java
import org.springframework.vault.core.VaultTemplate;

@Service
public class VaultSecretService {
    
    @Autowired
    private VaultTemplate vaultTemplate;
    
    public String getIngestionSecret() {
        return vaultTemplate.read("secret/langa")
            .getData()
            .get("secret")
            .toString();
    }
}
```

**4. Kubernetes Secrets**

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: langa-credentials
type: Opaque
data:
  app-key: YXBwX3h5ejEyMw==  # base64 encoded
  account-key: YWNjX2FiYzc4OQ==
  secret: c2VjX3lvdXJfc2VjcmV0
```

```yaml
# deployment.yaml
spec:
  containers:
  - name: myapp
    env:
    - name: LANGA_APP_KEY
      valueFrom:
        secretKeyRef:
          name: langa-credentials
          key: app-key
```

---

## Deployment

### How to Deploy with Docker

**Step 1: Create Dockerfile**

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/langa-backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Step 2: Build Image**

```bash
./mvnw clean package -DskipTests
docker build -t langa-backend:latest .
```

**Step 3: Run Container**

```bash
docker run -d \
  --name langa-backend \
  -p 8080:8080 \
  -e MONGODB_URI=mongodb://mongo:27017/langa \
  --env-file .env \
  -e CORS_ALLOWED_ORIGINS=http://localhost:3000 \
  langa-backend:latest
```

**Step 4: With Docker Compose**

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mongodb:
    image: mongo:7.0
    ports:
      - "27017:27017"
    volumes:
      - mongo-data:/data/db
  
  langa-backend:
    build: .
    ports:
      - "8080:8080"
    environment:
      - MONGODB_URI=mongodb://mongodb:27017/langa
      - AUTH_ISSUER_URI=https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0
      - AUTH_JWK_SET_URI=https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/discovery/v2.0/keys
      - AUTH_AUDIENCES=<langa-api client id>,api://<langa-api client id>
      - CORS_ALLOWED_ORIGINS=http://localhost:3000
    depends_on:
      - mongodb

volumes:
  mongo-data:
```

Run:

```bash
docker-compose up -d
```

---

### How to Deploy to Kubernetes

**Step 1: Create Secrets**

```bash
kubectl create secret generic langa-secrets \
  --from-literal=mongodb-uri='mongodb://mongo:27017/langa'
```

**Step 2: Create Deployment**

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: langa-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: langa-backend
  template:
    metadata:
      labels:
        app: langa-backend
    spec:
      containers:
      - name: langa-backend
        image: langa-backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: MONGODB_URI
          valueFrom:
            secretKeyRef:
              name: langa-secrets
              key: mongodb-uri
        - name: AUTH_ISSUER_URI
          value: "https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0"
        - name: AUTH_JWK_SET_URI
          value: "https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/discovery/v2.0/keys"
        - name: CORS_ALLOWED_ORIGINS
          value: "https://dashboard.example.com"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
```

**Step 3: Create Service**

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: langa-backend
spec:
  selector:
    app: langa-backend
  ports:
  - port: 80
    targetPort: 8080
  type: LoadBalancer
```

**Step 4: Deploy**

```bash
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

**Step 5: Verify**

```bash
kubectl get pods
kubectl logs -f deployment/langa-backend
kubectl get service langa-backend
```

---

### How to Configure for Production

**Environment Variables:**

```properties
# Database
MONGODB_URI=mongodb+srv://user:pass@cluster.mongodb.net/langa?retryWrites=true&w=majority

# Identity provider (see docs/authentication.md)
AUTH_ISSUER_URI=https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0
AUTH_JWK_SET_URI=https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/discovery/v2.0/keys
AUTH_AUDIENCES=<langa-api client id>,api://<langa-api client id>

# CORS
CORS_ALLOWED_ORIGINS=https://dashboard.example.com,https://app.example.com
CORS_ALLOW_CREDENTIALS=true

# Kafka (optional)
KAFKA_BOOTSTRAP_SERVERS=kafka-1:9092,kafka-2:9092,kafka-3:9092
KAFKA_TOPIC=langa-ingestion

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@example.com
MAIL_PASSWORD=app-specific-password
MAIL_SMTP_AUTH=true
MAIL_STARTTLS_ENABLE=true

# Logging
LOG_LEVEL_LANGA=INFO
LOG_LEVEL_SPRING_WEB=WARN
LOG_LEVEL_SPRING_SECURITY=WARN

# Base URL for emails
BASE_URL=https://api.example.com
```

**Security Checklist:**
- ✅ Use HTTPS (TLS certificates)
- ✅ Dev-token endpoint and Swagger disabled
- ✅ Secure MongoDB connection (authentication + TLS)
- ✅ Restrict CORS origins
- ✅ Use secrets management (not environment variables)
- ✅ Enable rate limiting
- ✅ Set up monitoring and alerts

---

## Monitoring and Maintenance

### How to Monitor Application Health

**Health Check Endpoint:**

```bash
curl http://localhost:8080/actuator/health
```

**Response (Healthy):**
```json
{
  "status": "UP",
  "components": {
    "mongo": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

**Kubernetes Health Probes:**

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 60
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 5
```

---

### How to View Application Metrics

**Prometheus Metrics:**

```bash
curl http://localhost:8080/actuator/prometheus
```

**Custom Metrics:**

```bash
curl http://localhost:8080/actuator/langaMetrics
```

**Example Metrics:**
- `langa.ingestion.logs.total` - Total logs ingested
- `langa.ingestion.metrics.total` - Total metrics ingested
- `langa.applications.count` - Number of applications
- `langa.users.count` - Number of users

---

### How to Enable Debug Logging

**Temporarily (Runtime):**

```bash
curl -X POST http://localhost:8080/actuator/loggers/com.langa.backend \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

**Permanently (Configuration):**

```properties
LOG_LEVEL_LANGA=DEBUG
```

**View Logs:**

```bash
# Docker
docker logs -f langa-backend

# Kubernetes
kubectl logs -f deployment/langa-backend

# Follow logs from multiple pods
kubectl logs -f -l app=langa-backend
```

---

### How to Backup MongoDB Data

**Using mongodump:**

```bash
mongodump --uri="mongodb://localhost:27017/langa" --out=/backup/$(date +%Y%m%d)
```

**Using MongoDB Atlas (Cloud):**
- Automated backups included
- Point-in-time recovery available
- Download backups via UI

**Kubernetes CronJob for Automated Backups:**

```yaml
apiVersion: batch/v1
kind: CronJob
metadata:
  name: mongodb-backup
spec:
  schedule: "0 2 * * *"  # Daily at 2 AM
  jobTemplate:
    spec:
      template:
        spec:
          containers:
          - name: backup
            image: mongo:7.0
            command:
            - /bin/sh
            - -c
            - mongodump --uri="${MONGODB_URI}" --out=/backup/$(date +%Y%m%d) && echo "Backup complete"
            env:
            - name: MONGODB_URI
              valueFrom:
                secretKeyRef:
                  name: langa-secrets
                  key: mongodb-uri
            volumeMounts:
            - name: backup
              mountPath: /backup
          volumes:
          - name: backup
            persistentVolumeClaim:
              claimName: backup-pvc
          restartPolicy: OnFailure
```

---

### How to Scale Horizontally

**Requirements for Horizontal Scaling:**
- ✅ Stateless application (bearer tokens, no sessions)
- ⚠️ Not yet safe with several instances: outbox poller and rate limiter are per instance (see roadmap)
- ✅ Shared database (MongoDB)
- ✅ No in-memory caching (or use Redis)

**Docker Compose:**

```bash
docker-compose up -d --scale langa-backend=3
```

**Kubernetes:**

```bash
kubectl scale deployment langa-backend --replicas=5
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
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

**Load Balancer Configuration:**

```nginx
# nginx.conf
upstream langa_backend {
    least_conn;
    server langa-1:8080;
    server langa-2:8080;
    server langa-3:8080;
}

server {
    listen 80;
    server_name api.example.com;
    
    location /api {
        proxy_pass http://langa_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tonyadji/langa/issues)
- **Email:** motodigo.appvenger@gmail.com

---

**End of How-To Guides**

For more information:
- [Tutorial](./02-TUTORIAL.md) - Step-by-step learning
- [Reference](./04-REFERENCE.md) - Complete API reference
- [Explanation](./05-EXPLANATION.md) - Understand the architecture
