package com.capricedumardi.agent.core.config.jmx;

public interface AgentManagementMBean {

  // --- 1. Runtime Tuning (Read / Write) ---
  // These are the parameters that can be changed without restarting

  boolean isDebugMode();
  void setDebugMode(boolean enabled);

  int getBufferBatchSize();
  void setBufferBatchSize(int size);

  int getBufferFlushIntervalSeconds();
  void setBufferFlushIntervalSeconds(int seconds);

  boolean isHttpCompressionEnabled();
  void enableHttpCompression(boolean enabled);
  int getHttpCompressionThresholdBytes();
  void setHttpCompressionThresholdBytes(int bytes);

  // --- 2. Static Configuration (Read Only) ---
  // Informational parameters (to verify loading)

  String getAgentVersion();
  String getLoggingFramework();
  String getIngestionUrl(); // To display, but may be partially masked

  // Buffer
  int getMainQueueCapacity();

  // HTTP Config
  int getHttpMaxConnectionsTotal();
  int getHttpMaxConnectionsPerRoute();
  int getHttpMaxRetryAttempts();

  // Circuit Breaker
  int getCircuitBreakerFailureThreshold();
  long getCircuitBreakerOpenDurationMillis();

  // Kafka (If used)
  boolean isKafkaAsyncSend();
  String getKafkaCompressionType();

  // --- 3. Config reloading ---
  void reloadConfig();
}