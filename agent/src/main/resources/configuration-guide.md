# Langa Agent - MVP Configuration Guide

## Overview

The Langa Agent MVP uses **explicit configuration** via environment variables to determine which logging framework to use. This approach is simple, predictable, and gives users full control.

---

## Configuration

### Required Environment Variables

| Variable | Description | Example Values |
|----------|-------------|----------------|
| `LOGGING_FRAMEWORK` | Which logging framework to use | `logback`, `log4j2`, `none` |
| `LANGA_APP_KEY` | Your application key | `app-12345` |
| `LANGA_ACCOUNT_KEY` | Your account key | `acc-67890` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `LANGA_BATCH_SIZE` | Number of entries to batch before sending | `50` |
| `LANGA_FLUSH_INTERVAL_SECONDS` | Seconds between automatic flushes | `5` |
| `LANGA_BACKEND_URL` | Backend ingestion endpoint | (from resolver) |

---

## Usage Examples

### Spring Boot Application (Logback)

Most Spring Boot apps use Logback by default.

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  spring-app:
    image: mycompany/spring-app:latest
    environment:
      - LOGGING_FRAMEWORK=logback
      - LANGA_APP_KEY=my-spring-app
      - LANGA_ACCOUNT_KEY=acc-12345
      - LANGA_BATCH_SIZE=100
      - LANGA_FLUSH_INTERVAL_SECONDS=10
    volumes:
      - ./langa-agent.jar:/opt/agents/langa-agent.jar
    command: java -javaagent:/opt/agents/langa-agent.jar -jar /app/spring-app.jar
```

**Standalone:**
```bash
export LOGGING_FRAMEWORK=logback
export LANGA_APP_KEY=my-spring-app
export LANGA_ACCOUNT_KEY=acc-12345

java -javaagent:langa-agent.jar -jar spring-app.jar
```

---

### Standalone Java App (Log4j2)

**docker-compose.yml:**
```yaml
version: '3.8'

services:
  java-app:
    image: mycompany/java-app:latest
    environment:
      - LOGGING_FRAMEWORK=log4j2
      - LANGA_APP_KEY=my-java-app
      - LANGA_ACCOUNT_KEY=acc-12345
    volumes:
      - ./langa-agent.jar:/opt/agents/langa-agent.jar
    command: java -javaagent:/opt/agents/langa-agent.jar -jar /app/java-app.jar
```

**Standalone:**
```bash
export LOGGING_FRAMEWORK=log4j2
export LANGA_APP_KEY=my-java-app
export LANGA_ACCOUNT_KEY=acc-12345

java -javaagent:langa-agent.jar -jar java-app.jar
```

---

### Metrics Only (No Log Collection)

If you only want metrics without log collection:

```bash
export LOGGING_FRAMEWORK=none
export LANGA_APP_KEY=metrics-only-app
export LANGA_ACCOUNT_KEY=acc-12345

java -javaagent:langa-agent.jar -jar app.jar
```

Or simply don't set `LOGGING_FRAMEWORK` and the agent will skip log collection.

---

### Kubernetes Deployment

**deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: my-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: my-app
  template:
    metadata:
      labels:
        app: my-app
    spec:
      containers:
      - name: app
        image: mycompany/my-app:1.0.0
        env:
        - name: LOGGING_FRAMEWORK
          value: "logback"
        - name: LANGA_APP_KEY
          valueFrom:
            configMapKeyRef:
              name: langa-config
              key: app-key
        - name: LANGA_ACCOUNT_KEY
          valueFrom:
            secretKeyRef:
              name: langa-secrets
              key: account-key
        - name: LANGA_BATCH_SIZE
          value: "200"
        - name: LANGA_FLUSH_INTERVAL_SECONDS
          value: "15"
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

## Behavior Matrix

| LOGGING_FRAMEWORK | Logback on Classpath | Log4j2 on Classpath | Result |
|-------------------|---------------------|---------------------|--------|
| `logback` | ✅ Yes | ❌ No | ✅ Binds Logback appender |
| `logback` | ❌ No | ✅ Yes | ❌ Error, falls back to classpath detection → Log4j2 |
| `log4j2` | ❌ No | ✅ Yes | ✅ Binds Log4j2 appender |
| `log4j2` | ✅ Yes | ❌ No | ❌ Error, falls back to classpath detection → Logback |
| `none` | ✅ Yes | ✅ Yes | ✅ No log collection (metrics only) |
| (not set) | ✅ Yes | ❌ No | ✅ Auto-detects Logback |
| (not set) | ❌ No | ✅ Yes | ✅ Auto-detects Log4j2 |
| (not set) | ❌ No | ❌ No | ⚠️ No log collection (metrics only) |
| (not set) | ✅ Yes | ✅ Yes | ⚠️ Picks Logback (first in priority) |

---

## Startup Log Examples

### Successful Logback Setup
```
========================================
  Langa Observability Agent Starting
========================================
✓ Buffers and sender initialized
  Using explicitly configured framework: Logback
✓ Using Logback for log collection
✓ Spring detected → expecting @EnableAspectJAutoProxy for metrics
✓ Appender binding: LangaLogbackAppender bound successfully.
========================================
  Langa Agent Initialization Complete
========================================
```

### Successful Log4j2 Setup
```
========================================
  Langa Observability Agent Starting
========================================
✓ Buffers and sender initialized
  Using explicitly configured framework: Log4j2
✓ Using Log4j2 for log collection
✓ Spring NOT detected → using AspectJ weaver for metrics
✓ Appender binding: LangaLog4jAppender bound successfully.
========================================
  Langa Agent Initialization Complete
========================================
```

### Classpath Fallback
```
========================================
  Langa Observability Agent Starting
========================================
✓ Buffers and sender initialized
  LOGGING_FRAMEWORK not set, attempting classpath detection...
  Detected Logback on classpath
✓ Using Logback for log collection
✓ Spring detected → expecting @EnableAspectJAutoProxy for metrics
========================================
  Langa Agent Initialization Complete
========================================
```

### Metrics Only
```
========================================
  Langa Observability Agent Starting
========================================
✓ Buffers and sender initialized
  Log collection explicitly disabled via LOGGING_FRAMEWORK=none
⚠ Log collection disabled (no logging framework configured)
  Metrics collection will still work
  To enable logs, set environment variable:
    LOGGING_FRAMEWORK=logback  (or log4j2)
✓ Spring NOT detected → using AspectJ weaver for metrics
========================================
  Langa Agent Initialization Complete
========================================
```

### Configuration Error
```
========================================
  Langa Observability Agent Starting
========================================
✓ Buffers and sender initialized
✗ ERROR: LOGGING_FRAMEWORK=logback but Logback not found on classpath!
  Falling back to classpath detection...
  Detected Log4j2 on classpath
✓ Using Log4j2 for log collection
✓ Spring detected → expecting @EnableAspectJAutoProxy for metrics
========================================
  Langa Agent Initialization Complete
========================================
```

---

## Troubleshooting

### Issue: "No supported logging framework found on classpath"

**Cause**: Neither Logback nor Log4j2 is on your application's classpath.

**Solutions:**
1. Add Logback dependency:
   ```xml
   <dependency>
       <groupId>ch.qos.logback</groupId>
       <artifactId>logback-classic</artifactId>
       <version>1.4.11</version>
   </dependency>
   ```

2. Or add Log4j2 dependency:
   ```xml
   <dependency>
       <groupId>org.apache.logging.log4j</groupId>
       <artifactId>log4j-core</artifactId>
       <version>2.20.0</version>
   </dependency>
   ```

3. Or use metrics-only mode: `LOGGING_FRAMEWORK=none`

### Issue: "ERROR: LOGGING_FRAMEWORK=logback but Logback not found"

**Cause**: You specified Logback but it's not on the classpath.

**Solutions:**
1. Change to `LOGGING_FRAMEWORK=log4j2` if you're using Log4j2
2. Add Logback to your dependencies
3. Remove `LOGGING_FRAMEWORK` to use auto-detection

### Issue: Logs not appearing in Langa backend

**Checklist:**
1. ✅ Check startup logs confirm appender was bound
2. ✅ Verify `LANGA_APP_KEY` and `LANGA_ACCOUNT_KEY` are set correctly
3. ✅ Check network connectivity to backend
4. ✅ Verify buffer isn't configured too large (try `LANGA_BATCH_SIZE=10`)
5. ✅ Check application is actually logging (test with a simple log statement)

---

## Migration Path (Future Versions)

For future versions, you can enhance this with:

1. **Auto-detection improvements**: Active detection of initialized frameworks
2. **Configuration file**: `langa-agent.properties` or `langa.yaml`
3. **Spring Boot auto-configuration**: Automatic setup for Spring Boot apps
4. **Multiple appender support**: Capture from multiple frameworks simultaneously

But for MVP, this explicit approach is **simple, reliable, and production-ready**.

---

## Best Practices

1. **Always set `LOGGING_FRAMEWORK` explicitly** in production for predictability
2. **Use `none` for testing** when you only need metrics
3. **Set appropriate batch sizes** based on log volume:
    - Low volume (< 100 logs/sec): `LANGA_BATCH_SIZE=50`
    - Medium volume (100-1000 logs/sec): `LANGA_BATCH_SIZE=200`
    - High volume (> 1000 logs/sec): `LANGA_BATCH_SIZE=500`
4. **Monitor agent startup logs** to verify correct configuration
5. **Use environment-specific configs** (dev vs staging vs prod)