# Langa Agent - Complete Configuration Guide

## Overview

The Langa Agent uses **flexible, multi-source configuration** to give you complete control over observability collection. Configuration can be provided via environment variables, configuration files, or system properties with clear priority rules.

**Configuration Priority** (highest to lowest):
1. System Properties (`-Dlanga.xxx=value`)
2. Environment Variables (`LANGA_XXX=value`)
3. Configuration File (`langa-agent.properties`)
4. Built-in Defaults

---

## Core Configuration

### Required Environment Variables

| Variable | Description | Example Values |
|----------|-------------|----------------|
| `LOGGING_FRAMEWORK` | Which logging framework to use | `logback`, `log4j2`, `none` |
| `LANGA_URL` | Backend ingestion URL | `http://api.langa.io/api/ingestion/h/base64creds` |
| `LANGA_SECRET` | Authentication secret | `your-secret-key` |

**Note**: The agent will **fail fast** with clear error messages if these are missing or invalid.

---

## Configuration Methods

### Method 1: Environment Variables (Recommended for Docker/K8s)

```bash
# Core configuration
export LOGGING_FRAMEWORK=logback
export LANGA_URL=http://api.langa.io/api/ingestion/h/base64creds
export LANGA_SECRET=your-secret-key

# Buffer configuration
export LANGA_BUFFER_BATCH_SIZE=100
export LANGA_BUFFER_FLUSH_INTERVAL_SECONDS=10
export LANGA_BUFFER_MAIN_QUEUE_CAPACITY=20000

# HTTP configuration
export LANGA_HTTP_MAX_CONNECTIONS_TOTAL=200
export LANGA_HTTP_COMPRESSION_THRESHOLD_BYTES=2048
export LANGA_HTTP_MAX_RETRY_ATTEMPTS=3

# Kafka configuration (if using Kafka)
export LANGA_KAFKA_ASYNC_SEND=true
export LANGA_KAFKA_COMPRESSION_TYPE=snappy

# Circuit breaker
export LANGA_CIRCUIT_BREAKER_FAILURE_THRESHOLD=5
export LANGA_CIRCUIT_BREAKER_OPEN_DURATION_MILLIS=30000

# Debug mode
export LANGA_DEBUG_MODE=false

java -javaagent:langa-agent.jar -jar app.jar
```

---

### Method 2: Configuration File (Recommended for Production)

Create `langa-agent.properties` in one of these locations:
- `./langa-agent.properties` (current directory)
- `/etc/langa/agent.properties` (system-wide)
- `~/.langa/agent.properties` (user-specific)

**langa-agent.properties:**
```properties
# ========================================
# Buffer Configuration
# ========================================
langa.buffer.batch.size=100
langa.buffer.flush.interval.seconds=10
langa.buffer.main.queue.capacity=20000
langa.buffer.retry.queue.capacity=10000

# ========================================
# HTTP Configuration
# ========================================
langa.http.max.connections.total=200
langa.http.max.connections.per.route=50
langa.http.connect.timeout.millis=5000
langa.http.socket.timeout.millis=10000
langa.http.compression.threshold.bytes=1024
langa.http.max.retry.attempts=3
langa.http.base.retry.delay.millis=100
langa.http.max.retry.delay.millis=5000

# ========================================
# Kafka Configuration
# ========================================
langa.kafka.async.send=true
langa.kafka.compression.type=snappy
langa.kafka.batch.size.bytes=16384
langa.kafka.acks=all
langa.kafka.retries=3

# ========================================
# Circuit Breaker
# ========================================
langa.circuit.breaker.failure.threshold=5
langa.circuit.breaker.open.duration.millis=30000

# ========================================
# Agent Settings
# ========================================
langa.debug.mode=false
langa.agent.version=1.0.0
```

---

### Method 3: System Properties (Quick Testing)

```bash
java -Dlanga.buffer.batch.size=100 \
     -Dlanga.http.max.connections.total=150 \
     -Dlanga.debug.mode=true \
     -javaagent:langa-agent.jar \
     -jar app.jar
```

---

## Complete Configuration Reference

### Buffer Configuration

Controls batching and queue behavior for logs and metrics.

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.buffer.batch.size` | `LANGA_BUFFER_BATCH_SIZE` | `50` | Entries to batch before sending |
| `langa.buffer.flush.interval.seconds` | `LANGA_BUFFER_FLUSH_INTERVAL_SECONDS` | `5` | Seconds between automatic flushes |
| `langa.buffer.main.queue.capacity` | `LANGA_BUFFER_MAIN_QUEUE_CAPACITY` | `10000` | Main queue size (prevents OOM) |
| `langa.buffer.retry.queue.capacity` | `LANGA_BUFFER_RETRY_QUEUE_CAPACITY` | `5000` | Retry queue size |

**Tuning Guidelines**:
- **Low volume** (< 100 logs/sec): batch=25, interval=10
- **Medium volume** (100-1000 logs/sec): batch=100, interval=5
- **High volume** (> 1000 logs/sec): batch=500, interval=2

---

### Scheduler Configuration

Controls thread pool for background tasks (buffer flushing, retries).

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.scheduler.thread.pool.size` | `LANGA_SCHEDULER_THREAD_POOL_SIZE` | `max(2, CPU/2)` | Number of scheduler threads |
| `langa.scheduler.shutdown.timeout.seconds` | `LANGA_SCHEDULER_SHUTDOWN_TIMEOUT_SECONDS` | `30` | Graceful shutdown timeout |

---

### HTTP Configuration

Controls HTTP sender behavior (connection pooling, timeouts, compression, retries).

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.http.max.connections.total` | `LANGA_HTTP_MAX_CONNECTIONS_TOTAL` | `100` | Total HTTP connection pool size |
| `langa.http.max.connections.per.route` | `LANGA_HTTP_MAX_CONNECTIONS_PER_ROUTE` | `20` | Max connections per route |
| `langa.http.connect.timeout.millis` | `LANGA_HTTP_CONNECT_TIMEOUT_MILLIS` | `5000` | Connection timeout (5s) |
| `langa.http.socket.timeout.millis` | `LANGA_HTTP_SOCKET_TIMEOUT_MILLIS` | `10000` | Socket read timeout (10s) |
| `langa.http.connection.request.timeout.millis` | `LANGA_HTTP_CONNECTION_REQUEST_TIMEOUT_MILLIS` | `30000` | Connection request timeout |
| `langa.http.compression.threshold.bytes` | `LANGA_HTTP_COMPRESSION_THRESHOLD_BYTES` | `1024` | GZIP compress if payload > this (1KB) |
| `langa.http.max.retry.attempts` | `LANGA_HTTP_MAX_RETRY_ATTEMPTS` | `3` | Max retry attempts |
| `langa.http.base.retry.delay.millis` | `LANGA_HTTP_BASE_RETRY_DELAY_MILLIS` | `100` | Base retry delay (exponential backoff) |
| `langa.http.max.retry.delay.millis` | `LANGA_HTTP_MAX_RETRY_DELAY_MILLIS` | `5000` | Max retry delay cap |

**HTTP Performance Notes**:
- ✅ **Automatic GZIP compression** for payloads > 1KB (90% bandwidth reduction)
- ✅ **Connection pooling** prevents connection overhead
- ✅ **Smart retry** with exponential backoff for transient failures
- ✅ **Circuit breaker** prevents wasting time on dead backends

---

### Kafka Configuration

Controls Kafka producer behavior (batching, compression, reliability).

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.kafka.request.timeout.millis` | `LANGA_KAFKA_REQUEST_TIMEOUT_MILLIS` | `30000` | Request timeout (30s) |
| `langa.kafka.delivery.timeout.millis` | `LANGA_KAFKA_DELIVERY_TIMEOUT_MILLIS` | `120000` | Delivery timeout (2min) |
| `langa.kafka.producer.close.timeout.seconds` | `LANGA_KAFKA_PRODUCER_CLOSE_TIMEOUT_SECONDS` | `10` | Producer close timeout |
| `langa.kafka.batch.size.bytes` | `LANGA_KAFKA_BATCH_SIZE_BYTES` | `16384` | Batch size (16KB) |
| `langa.kafka.linger.millis` | `LANGA_KAFKA_LINGER_MILLIS` | `10` | Wait for batching (10ms) |
| `langa.kafka.buffer.memory.bytes` | `LANGA_KAFKA_BUFFER_MEMORY_BYTES` | `33554432` | Buffer memory (32MB) |
| `langa.kafka.compression.type` | `LANGA_KAFKA_COMPRESSION_TYPE` | `snappy` | Compression: snappy, gzip, lz4, zstd, none |
| `langa.kafka.acks` | `LANGA_KAFKA_ACKS` | `all` | Ack level: all, 1, 0 |
| `langa.kafka.retries` | `LANGA_KAFKA_RETRIES` | `3` | Retry attempts |
| `langa.kafka.max.in.flight.requests` | `LANGA_KAFKA_MAX_IN_FLIGHT_REQUESTS` | `5` | Max in-flight requests |
| `langa.kafka.enable.idempotence` | `LANGA_KAFKA_ENABLE_IDEMPOTENCE` | `true` | Enable exactly-once |
| `langa.kafka.async.send` | `LANGA_KAFKA_ASYNC_SEND` | `true` | Async mode (with accurate tracking) |

**Kafka Performance Notes**:
- ✅ **Async send with callback tracking** - returns accurate success/failure
- ✅ **Proper lifecycle management** - producer closes cleanly on shutdown
- ✅ **Exactly-once semantics** with idempotence enabled
- ✅ **Automatic compression** with Snappy by default

---

### Circuit Breaker Configuration

Prevents wasting resources on dead backends. After N consecutive failures, circuit opens and rejects requests immediately for a timeout period.

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.circuit.breaker.failure.threshold` | `LANGA_CIRCUIT_BREAKER_FAILURE_THRESHOLD` | `5` | Failures before opening circuit |
| `langa.circuit.breaker.open.duration.millis` | `LANGA_CIRCUIT_BREAKER_OPEN_DURATION_MILLIS` | `30000` | Wait before testing recovery (30s) |

**Circuit Breaker States**:
```
CLOSED (normal) → 5 failures → OPEN (rejecting) → 30s → HALF_OPEN (testing) → success → CLOSED
                                        ↑                                            ↓
                                        └──────────────── failure ──────────────────┘
```

---

### Retry Configuration

Generic retry settings for buffer operations.

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.retry.max.delay.seconds` | `LANGA_RETRY_MAX_DELAY_SECONDS` | `300` | Max retry delay (5 min) |
| `langa.retry.max.consecutive.errors` | `LANGA_RETRY_MAX_CONSECUTIVE_ERRORS` | `10` | Max errors before giving up |

---

### Agent Metadata

| Property | Env Variable | Default | Description |
|----------|-------------|---------|-------------|
| `langa.agent.version` | `LANGA_AGENT_VERSION` | `1.0.0` | Agent version (for User-Agent header) |
| `langa.debug.mode` | `LANGA_DEBUG_MODE` | `false` | Enable verbose debug output |

---

## Usage Examples

### Spring Boot Application (Logback + HTTP)

Most Spring Boot apps use Logback by default.

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  spring-app:
    image: mycompany/spring-app:latest
    environment:
      # Core config
      - LOGGING_FRAMEWORK=logback
      - LANGA_URL=https://api.langa.io/api/ingestion/h/YWNjLWtleS1sZ2EtYXBwLWtleQ==
      - LANGA_SECRET=your-secret-key
      
      # Buffer tuning
      - LANGA_BUFFER_BATCH_SIZE=100
      - LANGA_BUFFER_FLUSH_INTERVAL_SECONDS=10
      - LANGA_BUFFER_MAIN_QUEUE_CAPACITY=20000
      
      # HTTP tuning
      - LANGA_HTTP_MAX_CONNECTIONS_TOTAL=200
      - LANGA_HTTP_COMPRESSION_THRESHOLD_BYTES=512
      
      # Circuit breaker
      - LANGA_CIRCUIT_BREAKER_FAILURE_THRESHOLD=5
      
    volumes:
      - ./langa-agent.jar:/opt/agents/langa-agent.jar
    command: java -javaagent:/opt/agents/langa-agent.jar -jar /app/spring-app.jar
```

---

### High-Volume Application (Log4j2 + Kafka)

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  high-volume-app:
    image: mycompany/high-volume-app:latest
    environment:
      # Core config
      - LOGGING_FRAMEWORK=log4j2
      - LANGA_URL=kafka://kafka-broker:9092/api/ingestion/langa-logs/YWNjLWtleS1sZ2EtYXBwLWtleQ==
      - LANGA_SECRET=your-secret-key
      
      # Buffer tuning for high volume
      - LANGA_BUFFER_BATCH_SIZE=500
      - LANGA_BUFFER_FLUSH_INTERVAL_SECONDS=2
      - LANGA_BUFFER_MAIN_QUEUE_CAPACITY=50000
      
      # Kafka tuning
      - LANGA_KAFKA_BATCH_SIZE_BYTES=32768
      - LANGA_KAFKA_LINGER_MILLIS=20
      - LANGA_KAFKA_COMPRESSION_TYPE=snappy
      - LANGA_KAFKA_ASYNC_SEND=true
      
    volumes:
      - ./langa-agent.jar:/opt/agents/langa-agent.jar
    command: java -javaagent:/opt/agents/langa-agent.jar -jar /app/app.jar
```

---

### Kubernetes Deployment with ConfigMap

**configmap.yaml:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: langa-config
data:
  LOGGING_FRAMEWORK: "logback"
  
  # Buffer config
  LANGA_BUFFER_BATCH_SIZE: "200"
  LANGA_BUFFER_FLUSH_INTERVAL_SECONDS: "5"
  LANGA_BUFFER_MAIN_QUEUE_CAPACITY: "30000"
  
  # HTTP config
  LANGA_HTTP_MAX_CONNECTIONS_TOTAL: "150"
  LANGA_HTTP_COMPRESSION_THRESHOLD_BYTES: "1024"
  LANGA_HTTP_MAX_RETRY_ATTEMPTS: "3"
  
  # Circuit breaker
  LANGA_CIRCUIT_BREAKER_FAILURE_THRESHOLD: "3"
  LANGA_CIRCUIT_BREAKER_OPEN_DURATION_MILLIS: "30000"
  
  # Debug
  LANGA_DEBUG_MODE: "false"
```

**secret.yaml:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: langa-secrets
type: Opaque
stringData:
  LANGA_URL: "https://api.langa.io/api/ingestion/h/YWNjLWtleS1sZ2EtYXBwLWtleQ=="
  LANGA_SECRET: "your-secret-key"
```

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: app
        image: mycompany/my-app:1.0.0
        envFrom:
        - configMapRef:
            name: langa-config
        - secretRef:
            name: langa-secrets
        volumeMounts:
        - name: langa-agent
          mountPath: /opt/agents
        command:
        - java
        - -javaagent:/opt/agents/langa-agent.jar
        - -jar
        - /app/my-app.jar
      volumes:
      - name: langa-agent
        configMap:
          name: langa-agent-jar
```

---

### Metrics Only (No Log Collection)

```bash
export LOGGING_FRAMEWORK=none
export LANGA_URL=http://api.langa.io/api/ingestion/h/base64creds
export LANGA_SECRET=your-secret-key
export LANGA_BUFFER_BATCH_SIZE=200

java -javaagent:langa-agent.jar -jar app.jar
```

---

## Configuration Profiles

### Development Profile

Fast feedback, verbose logging, small batches.

```properties
# Fast feedback
langa.buffer.batch.size=10
langa.buffer.flush.interval.seconds=2

# Verbose output
langa.debug.mode=true

# Aggressive circuit breaker for fast failure detection
langa.circuit.breaker.failure.threshold=3

# Fewer retries
langa.http.max.retry.attempts=1
```

---

### Production Profile

Optimized for throughput and reliability.

```properties
# Larger batches for efficiency
langa.buffer.batch.size=200
langa.buffer.flush.interval.seconds=5
langa.buffer.main.queue.capacity=50000

# Quiet logs
langa.debug.mode=false

# More connections for high throughput
langa.http.max.connections.total=200
langa.http.max.connections.per.route=50

# Aggressive compression
langa.http.compression.threshold.bytes=512

# Standard circuit breaker
langa.circuit.breaker.failure.threshold=5
langa.circuit.breaker.open.duration.millis=30000
```

---

### High-Volume Profile

Tuned for extreme throughput (1000+ logs/sec).

```properties
# Very large batches
langa.buffer.batch.size=500
langa.buffer.flush.interval.seconds=2
langa.buffer.main.queue.capacity=100000
langa.buffer.retry.queue.capacity=50000

# Maximum connections
langa.http.max.connections.total=300
langa.http.max.connections.per.route=100

# Kafka optimizations
langa.kafka.batch.size.bytes=65536
langa.kafka.linger.millis=50
langa.kafka.buffer.memory.bytes=67108864

# Larger thread pool
langa.scheduler.thread.pool.size=8
```

---

## Startup Log Examples

### Successful HTTP Setup
```
Loading Langa Agent configuration...
  No config file found
  Using environment variables and system properties
✓ Configuration loaded successfully

========================================
  Langa Observability Agent Starting
========================================
Creating SenderService from configuration...
  Configuration validated successfully
  Sender type: HTTP
  HTTP URL: https://api.langa.io/api/ingestion
HttpSenderService initialized: https://api.langa.io/api/ingestion
✓ SenderService created: HTTP[https://api.langa.io/api/ingestion, circuit=CLOSED]
✓ Buffers and sender initialized
  Using explicitly configured framework: Logback
✓ Using Logback for log collection
✓ Appender binding: LangaLogbackAppender bound successfully.
========================================
  Langa Agent Initialization Complete
========================================
```

---

### Successful Kafka Setup
```
Loading Langa Agent configuration...
✓ Loaded config from: /etc/langa/agent.properties
✓ Configuration loaded successfully

========================================
  Langa Observability Agent Starting
========================================
Creating SenderService from configuration...
  Configuration validated successfully
  Sender type: KAFKA
  Bootstrap Server: kafka-broker:9092
  Topic: langa-logs
KafkaSenderService initialized:
  Bootstrap: kafka-broker:9092
  Topic: langa-logs
  Mode: ASYNC
✓ SenderService created: Kafka[langa-logs, mode=ASYNC, circuit=CLOSED]
✓ Buffers and sender initialized
  Using explicitly configured framework: Log4j2
✓ Using Log4j2 for log collection
✓ Appender binding: LangaLog4jAppender bound successfully.
========================================
  Langa Agent Initialization Complete
========================================
```

---

### Configuration Error (Fail-Fast)
```
Loading Langa Agent configuration...
✓ Configuration loaded successfully

========================================
  Langa Observability Agent Starting
========================================
Creating SenderService from configuration...
✗ FATAL: Failed to create SenderService
  Reason: LANGA_URL is required but not configured. Set environment variable: LANGA_URL=<your-ingestion-url>
  Impact: Agent will NOT send logs/metrics
  Action: Fix configuration and restart

Exception in thread "main" java.lang.IllegalArgumentException: SenderService configuration invalid: LANGA_URL is required but not configured.
```

---

### Circuit Breaker in Action
```
HttpSenderService: Server error (500), will retry
HttpSenderService: Server error (500), will retry
HttpSenderService: Server error (500), will retry
HttpSenderService: Server error (500), will retry
HttpSenderService: Server error (500), will retry
CircuitBreaker[HTTP[https://api.langa.io]]: CLOSED -> OPEN (threshold exceeded: 5 failures)

[30 seconds later]
CircuitBreaker[HTTP[https://api.langa.io]]: OPEN -> HALF_OPEN (testing recovery)
HttpSenderService: Send successful
CircuitBreaker[HTTP[https://api.langa.io]]: HALF_OPEN -> CLOSED (recovery confirmed)
```

---

## Behavior Matrix

### Logging Framework Configuration

| LOGGING_FRAMEWORK | Logback on Classpath | Log4j2 on Classpath | Result |
|-------------------|---------------------|---------------------|--------|
| `logback` | ✅ Yes | ❌ No | ✅ Binds Logback appender |
| `logback` | ❌ No | ✅ Yes | ❌ Error, falls back → Log4j2 |
| `log4j2` | ❌ No | ✅ Yes | ✅ Binds Log4j2 appender |
| `log4j2` | ✅ Yes | ❌ No | ❌ Error, falls back → Logback |
| `none` | ✅ Yes | ✅ Yes | ✅ No log collection (metrics only) |
| (not set) | ✅ Yes | ❌ No | ✅ Auto-detects Logback |
| (not set) | ❌ No | ✅ Yes | ✅ Auto-detects Log4j2 |
| (not set) | ❌ No | ❌ No | ⚠️ No log collection (metrics only) |
| (not set) | ✅ Yes | ✅ Yes | ⚠️ Picks Logback (first in priority) |

### Sender Type Configuration

| LANGA_URL Pattern | Result |
|-------------------|--------|
| `http://...` or `https://...` | HTTP sender with connection pooling + GZIP |
| `kafka://bootstrap:port/...` | Kafka sender with async tracking |
| Invalid or missing | ❌ Fail-fast with clear error |

---

## Troubleshooting

### Issue: "SenderService configuration invalid: LANGA_URL is required"

**Cause**: Missing required LANGA_URL environment variable.

**Fix**:
```bash
export LANGA_URL=http://api.langa.io/api/ingestion/h/base64creds
export LANGA_SECRET=your-secret-key
```

---

### Issue: "No supported logging framework found on classpath"

**Cause**: Neither Logback nor Log4j2 is available.

**Solutions:**
1. Add Logback:
   ```xml
   <dependency>
       <groupId>ch.qos.logback</groupId>
       <artifactId>logback-classic</artifactId>
       <version>1.4.11</version>
   </dependency>
   ```

2. Or use metrics-only: `LOGGING_FRAMEWORK=none`

---

### Issue: Logs not appearing in backend

**Checklist:**
1. ✅ Check startup logs confirm appender was bound
2. ✅ Verify circuit breaker state: `circuit=CLOSED` (not OPEN)
3. ✅ Check batch size isn't too large: try `LANGA_BUFFER_BATCH_SIZE=10`
4. ✅ Verify credentials are correct
5. ✅ Check network connectivity
6. ✅ Enable debug mode: `LANGA_DEBUG_MODE=true`

---

### Issue: High memory usage

**Causes**: Queue sizes too large or batch size too small.

**Solutions:**
```bash
# Reduce queue sizes
export LANGA_BUFFER_MAIN_QUEUE_CAPACITY=5000
export LANGA_BUFFER_RETRY_QUEUE_CAPACITY=2000

# Increase batch size (send more frequently)
export LANGA_BUFFER_BATCH_SIZE=200
export LANGA_BUFFER_FLUSH_INTERVAL_SECONDS=3
```

---

### Issue: "Circuit breaker always OPEN"

**Cause**: Backend is actually down or unreachable.

**Debug**:
```bash
# Enable debug mode
export LANGA_DEBUG_MODE=true

# Check connectivity manually
curl -X POST $LANGA_URL \
  -H "Content-Type: application/json" \
  -d '{"test": "data"}'
```

**Wait for automatic recovery**: Circuit opens for 30s by default, then tests recovery.

---

### Issue: JVM hangs on shutdown

**Cause**: Using old version without proper resource cleanup.

**Fix**: Ensure you're using the improved service layer with:
- Instance-level HttpClient (not static)
- Kafka producer properly closes
- BuffersFactory calls `sender.close()`

---

### Issue: "Invalid integer value for langa.buffer.batch.size: abc"

**Cause**: Invalid configuration value.

**Fix**: Provide valid integer:
```bash
export LANGA_BUFFER_BATCH_SIZE=100  # Not "abc"
```

---

## Performance Tuning

### Network Bandwidth

**Problem**: High bandwidth usage

**Solution**: Lower compression threshold
```bash
export LANGA_HTTP_COMPRESSION_THRESHOLD_BYTES=512  # Compress more aggressively
```

**Impact**: 90% bandwidth reduction with GZIP

---

### Latency

**Problem**: High latency sending logs

**Solution**: Increase batch size, reduce flush interval
```bash
export LANGA_BUFFER_BATCH_SIZE=200
export LANGA_BUFFER_FLUSH_INTERVAL_SECONDS=3
```

---

### Throughput

**Problem**: Can't keep up with log volume

**Solution**: Increase connections and batch sizes
```bash
export LANGA_HTTP_MAX_CONNECTIONS_TOTAL=300
export LANGA_HTTP_MAX_CONNECTIONS_PER_ROUTE=100
export LANGA_BUFFER_BATCH_SIZE=500
export LANGA_SCHEDULER_THREAD_POOL_SIZE=8
```

---

## Best Practices

1. ✅ **Always set LANGA_URL and LANGA_SECRET** - Agent fails fast if missing
2. ✅ **Set LOGGING_FRAMEWORK explicitly** in production for predictability
3. ✅ **Use config file for base settings**, override with env vars per environment
4. ✅ **Start with defaults**, tune based on actual metrics
5. ✅ **Monitor circuit breaker state** - OPEN means backend issues
6. ✅ **Enable debug mode temporarily** to troubleshoot issues
7. ✅ **Use appropriate profile** (dev/prod/high-volume) as starting point
8. ✅ **Test configuration** in staging before production
9. ✅ **Document custom values** in your deployment documentation
10. ✅ **Monitor agent resource usage** (CPU, memory, network)

---

## What's New in This Version

### Service Layer Improvements
- ✅ **Proper resource lifecycle** - All senders properly close on shutdown
- ✅ **HTTP GZIP compression** - 90% bandwidth reduction for large payloads
- ✅ **Smart retry logic** - Exponential backoff for transient failures
- ✅ **Circuit breaker pattern** - Fast rejection when backend is down
- ✅ **Accurate Kafka async tracking** - Returns actual success/failure
- ✅ **Instance-level HTTP client** - Fixed resource leak
- ✅ **Fail-fast validation** - Clear errors for invalid configuration

### Configuration System
- ✅ **Multi-source configuration** - File, env vars, system properties
- ✅ **Clear priority rules** - System props > env vars > file > defaults
- ✅ **All parameters configurable** - No more hardcoded values
- ✅ **Comprehensive documentation** - Examples for every scenario

---

## Files

All example configurations and documentation available at:
- `langa-agent.properties.example` - Complete example config file
- `configuration-guide.md` - Detailed reference guide

Built with ❤️ par [Caprice du Mardi](https://github.com/langa-org)