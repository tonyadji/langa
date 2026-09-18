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

**Prerequisites:** Authenticated user with valid JWT token

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

### How to Implement HTTP Ingestion in Java

**Use Case:** Send logs from a Java Spring Boot application

**Step 1: Add Dependencies**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
```

**Step 2: Create Ingestion Client**

```java
import org.apache.commons.codec.binary.Hex;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class LangaIngestionClient {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String appKey;
    private final String accountKey;
    private final String secret;
    private final String ingestionUrl;
    private static final String USER_AGENT = "java-client/1.0";
    
    public LangaIngestionClient(
            @Value("${langa.app-key}") String appKey,
            @Value("${langa.account-key}") String accountKey,
            @Value("${langa.secret}") String secret,
            @Value("${langa.ingestion-url}") String ingestionUrl) {
        this.appKey = appKey;
        this.accountKey = accountKey;
        this.secret = secret;
        this.ingestionUrl = ingestionUrl;
    }
    
    public void sendLogs(List<LogDto> logs) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String body = toJson(Map.of("logs", logs, "metrics", List.of()));
        String signature = calculateSignature(timestamp, body);
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-USER-AGENT", USER_AGENT);
        headers.set("X-APP-KEY", appKey);
        headers.set("X-ACCOUNT-KEY", accountKey);
        headers.set("X-TIMESTAMP", timestamp);
        headers.set("X-AGENT-SIGNATURE", signature);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(ingestionUrl, request, Void.class);
    }
    
    private String calculateSignature(String timestamp, String body) {
        try {
            String payload = USER_AGENT + appKey + accountKey + timestamp + body;
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate signature", e);
        }
    }
    
    private String toJson(Object obj) {
        // Use Jackson or Gson
        return new ObjectMapper().writeValueAsString(obj);
    }
}
```

**Step 3: Create Log DTO**

```java
public record LogDto(
    String message,
    String level,
    String loggerName,
    String timestamp,
    String threadName,
    String stackTrace,
    Map<String, String> mdc
) {}
```

**Step 4: Send Logs**

```java
@Service
public class MyService {
    
    @Autowired
    private LangaIngestionClient langaClient;
    
    public void doWork() {
        try {
            // Your business logic
        } catch (Exception e) {
            langaClient.sendLogs(List.of(new LogDto(
                e.getMessage(),
                "ERROR",
                getClass().getName(),
                Instant.now().toString(),
                Thread.currentThread().getName(),
                getStackTrace(e),
                MDC.getCopyOfContextMap()
            )));
        }
    }
    
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
```

**Step 5: Configure Application Properties**

```properties
# application.properties
langa.app-key=app_xyz123
langa.account-key=acc_abc789
langa.secret=sec_your_secret
langa.ingestion-url=http://localhost:8080/api/ingestion
```

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
from typing import List, Dict, Optional

class LangaClient:
    def __init__(self, app_key: str, account_key: str, secret: str, ingestion_url: str):
        self.app_key = app_key
        self.account_key = account_key
        self.secret = secret
        self.ingestion_url = ingestion_url
        self.user_agent = "python-client/1.0"
    
    def send_logs(self, logs: List[Dict]) -> None:
        timestamp = str(int(time.time() * 1000))
        body = json.dumps({"logs": logs, "metrics": []})
        signature = self._calculate_signature(timestamp, body)
        
        headers = {
            "X-USER-AGENT": self.user_agent,
            "X-APP-KEY": self.app_key,
            "X-ACCOUNT-KEY": self.account_key,
            "X-TIMESTAMP": timestamp,
            "X-AGENT-SIGNATURE": signature,
            "Content-Type": "application/json"
        }
        
        response = requests.post(self.ingestion_url, data=body, headers=headers)
        response.raise_for_status()
    
    def _calculate_signature(self, timestamp: str, body: str) -> str:
        payload = f"{self.user_agent}{self.app_key}{self.account_key}{timestamp}{body}"
        signature = hmac.new(
            self.secret.encode('utf-8'),
            payload.encode('utf-8'),
            hashlib.sha256
        ).digest()
        return base64.b64encode(signature).decode('utf-8')
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
- Kafka cluster running
- Langa Backend configured with Kafka consumer

**Step 1: Configure Kafka Consumer in Backend**

```properties
# application.properties
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
KAFKA_TOPIC=langa-ingestion
KAFKA_CONSUMER_GROUP_ID=langa-backend
KAFKA_AUTO_OFFSET_RESET=earliest
KAFKA_ENABLE_AUTO_COMMIT=true
```

**Step 2: Create Kafka Producer (Java)**

```java
@Configuration
public class KafkaProducerConfig {
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}

@Service
public class LangaKafkaIngestion {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Value("${langa.kafka.topic}")
    private String topic;
    
    public void sendLogs(List<LogDto> logs) {
        IngestionRequest request = new IngestionRequest(logs, List.of());
        String json = toJson(request);
        kafkaTemplate.send(topic, json);
    }
    
    private String toJson(Object obj) {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
```

**Step 3: Send Messages**

```java
@Service
public class MyService {
    
    @Autowired
    private LangaKafkaIngestion ingestion;
    
    public void logEvent(String message) {
        ingestion.sendLogs(List.of(
            new LogDto(
                message,
                "INFO",
                getClass().getName(),
                Instant.now().toString(),
                Thread.currentThread().getName(),
                null,
                Map.of()
            )
        ));
    }
}
```

**Benefits:**
- Asynchronous (non-blocking)
- High throughput
- Built-in retry and fault tolerance
- Decoupled from Langa Backend availability

**Considerations:**
- Requires Kafka infrastructure
- No immediate confirmation of ingestion
- More complex setup than HTTP

---

### How to Batch Logs for Better Performance

**Problem:** Sending logs individually is inefficient

**Solution:** Buffer and batch logs

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

### How to Refresh an Expired Token

**Problem:** Access token expired (401 Unauthorized)

**Solution:**

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

**Response:**
```json
{
  "accessToken": "new_access_token",
  "refreshToken": "new_refresh_token",
  "email": "you@example.com"
}
```

**Notes:**
- Refresh tokens are single-use
- Old refresh token is invalidated
- New refresh token has extended expiration

**Best Practice:**
Refresh tokens before they expire using a timer:

```javascript
// JavaScript example
const TOKEN_EXPIRY_MS = 3600000; // 1 hour
const REFRESH_BEFORE_MS = 300000; // 5 minutes

setTimeout(() => {
  refreshAccessToken();
}, TOKEN_EXPIRY_MS - REFRESH_BEFORE_MS);
```

---

### How to Handle Authentication in a Dashboard

**Pattern:** Token Storage and Auto-Refresh

**Step 1: Store Tokens Securely**

```javascript
// localStorage (simple but less secure)
localStorage.setItem('accessToken', response.data.accessToken);
localStorage.setItem('refreshToken', response.data.refreshToken);

// sessionStorage (cleared on tab close)
sessionStorage.setItem('accessToken', response.data.accessToken);

// httpOnly cookie (most secure, requires backend support)
document.cookie = `accessToken=${token}; secure; httpOnly`;
```

**Step 2: Create Axios Interceptor**

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Add token to requests
api.interceptors.request.use(config => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Handle 401 and refresh
api.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      const refreshToken = localStorage.getItem('refreshToken');
      
      try {
        const response = await axios.post(
          'http://localhost:8080/api/auth/refresh',
          { refreshToken }
        );
        
        localStorage.setItem('accessToken', response.data.accessToken);
        localStorage.setItem('refreshToken', response.data.refreshToken);
        
        // Retry original request
        error.config.headers.Authorization = `Bearer ${response.data.accessToken}`;
        return axios(error.config);
      } catch (refreshError) {
        // Redirect to login
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    return Promise.reject(error);
  }
);

export default api;
```

**Step 3: Use the API Client**

```javascript
import api from './api';

async function getLogs(appId) {
  const response = await api.get(`/applications/${appId}/logs`);
  return response.data;
}
```

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
  -e JWT_KEY=your-secret-key \
  -e JWT_KID=langa \
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
      - JWT_KEY=change-this-in-production
      - JWT_KID=langa
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
  --from-literal=jwt-key='your-secret-key' \
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
        - name: JWT_KEY
          valueFrom:
            secretKeyRef:
              name: langa-secrets
              key: jwt-key
        - name: JWT_KID
          value: "langa"
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

# JWT (use strong random keys)
JWT_KEY=<64-character-random-string>
JWT_KID=langa-prod
JWT_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

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
- ✅ Strong JWT secret (64+ characters)
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
- ✅ Stateless application (uses JWT, not sessions)
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
