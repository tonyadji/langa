package com.capricedumardi.agent.core.config.jmx;

public interface AgentManagementMBean {

  // --- 1. Runtime Tuning (Lecture / Écriture) ---
  // Ce sont les paramètres que l'on peut changer sans redémarrer

  boolean isDebugMode();
  void setDebugMode(boolean enabled);

  int getBufferBatchSize();
  void setBufferBatchSize(int size);

  int getBufferFlushIntervalSeconds();
  void setBufferFlushIntervalSeconds(int seconds);

  boolean isHttpCompressionEnabled();
  void enableHttpCompression(boolean enabled);
  long getHttpCompressionThresholdBytes();
  void setHttpCompressionThresholdBytes(long bytes);

  // --- 2. Configuration Statique (Lecture Seule) ---
  // Paramètres informatifs (pour vérifier le chargement)

  String getAgentVersion();
  String getLoggingFramework();
  String getIngestionUrl(); // A afficher, mais peut-être masqué partiellement

  // Buffer
  int getMainQueueCapacity();

  // HTTP Config
  int getHttpMaxConnectionsTotal();
  int getHttpMaxConnectionsPerRoute();
  int getHttpMaxRetryAttempts();

  // Circuit Breaker
  int getCircuitBreakerFailureThreshold();
  long getCircuitBreakerOpenDurationMillis();

  // Kafka (Si utilisé)
  boolean isKafkaAsyncSend();
  String getKafkaCompressionType();

  // --- 3. Config reloading ---
  void reloadConfig();
}