package com.capricedumardi.agent.core.config.jmx;

import com.capricedumardi.agent.core.config.AgentConfig;
import com.capricedumardi.agent.core.config.ConfigLoader;
import com.capricedumardi.agent.core.config.LangaPrinter;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.StandardMBean;
import java.util.concurrent.atomic.AtomicInteger;

public class AgentDynamicConfig extends StandardMBean implements AgentDynamicConfigMBean {

  private static AgentDynamicConfig instance;

  // 1. Keep a reference to the immutable config (for what never changes)
  private final AgentConfig staticConfig;

  // 2. Define "Atomic" versions (modifiable) for what we want to tune
  private final AtomicInteger currentBatchSize;
  private final AtomicInteger currentFlushInterval;
  private final AtomicBoolean currentDebugMode;
  private final AtomicBoolean currentHttpCompressionEnabled;
  private final AtomicInteger currentCompressionThreshold;

  // Singleton Lazy-loading
  public static synchronized AgentDynamicConfig getInstance() {
    if (instance == null) {
      // Get your existing AgentConfig object
      AgentConfig initialConfig = ConfigLoader.getConfigInstance();
      try {
        instance = new AgentDynamicConfig(initialConfig);
      } catch (Exception e) {
        // Fallback ou erreur fatale selon ton choix
        LangaPrinter.printError("Failed to initialize AgentManagement JMX: " + e.getMessage());
      }
    }
    return instance;
  }

  private AgentDynamicConfig(AgentConfig config) {
    super(AgentDynamicConfigMBean.class, false);
    this.staticConfig = config;

    // 3. Initialize the dynamic values with the values from the initial config
    this.currentBatchSize = new AtomicInteger(config.getBatchSize());
    this.currentFlushInterval = new AtomicInteger(config.getFlushIntervalSeconds());
    this.currentDebugMode = new AtomicBoolean(config.isDebugMode());
    this.currentHttpCompressionEnabled = new AtomicBoolean(config.isHttpCompressionEnabled());
    this.currentCompressionThreshold = new AtomicInteger(config.getHttpCompressionThresholdBytes());

    registerJMX();
  }

  private void registerJMX() {
    try {
      java.lang.management.ManagementFactory.getPlatformMBeanServer()
          .registerMBean(this, new javax.management.ObjectName("com.capricedumardi.agent:type=AgentManagement"));
      LangaPrinter.printTrace("JMX MBean registered: com.capricedumardi.agent:type=AgentManagement");
    } catch (Exception e) {
      LangaPrinter.printError("Failed to register JMX: " + e.getMessage());
    }
  }

  // =========================================================================
  // PART 1: DYNAMIC (Read / Write via JMX)
  // =========================================================================

  @Override
  public int getBufferBatchSize() { return currentBatchSize.get(); }

  @Override
  public void setBufferBatchSize(int size) {
    if (size > 0) {
      currentBatchSize.set(size);
      LangaPrinter.printTrace("JMX Update: BatchSize changed to " + size);
    }
  }

  @Override
  public int getBufferFlushIntervalSeconds() { return currentFlushInterval.get(); }

  @Override
  public void setBufferFlushIntervalSeconds(int seconds) {
    if (seconds > 0) {
      currentFlushInterval.set(seconds);
      LangaPrinter.printTrace("JMX Update: FlushInterval changed to " + seconds + "s");
    }
  }

  @Override
  public boolean isHttpCompressionEnabled() {
    return currentHttpCompressionEnabled.get();
  }

  @Override
  public void enableHttpCompression(boolean enabled) {
    currentHttpCompressionEnabled.set(enabled);
    LangaPrinter.printTrace("JMX Update: HttpCompressionEnabled changed to " + enabled);
  }

  @Override
  public boolean isDebugMode() { return currentDebugMode.get(); }

  @Override
  public void setDebugMode(boolean enabled) {
    currentDebugMode.set(enabled);
    LangaPrinter.printTrace("JMX Update: DebugMode changed to " + enabled);
  }

  @Override
  public int getHttpCompressionThresholdBytes() {
    return currentCompressionThreshold.get();
  }

  @Override
  public void setHttpCompressionThresholdBytes(int bytes) {
    if (bytes > 0) {
      currentCompressionThreshold.set(bytes);
      LangaPrinter.printTrace("JMX Update: HttpCompressionThresholdBytes changed to " + bytes);
    }
  }

  // =========================================================================
  // PART 2: STATIC (Read only - Delegation to AgentConfig)
  // =========================================================================

  // These methods are exposed in JMX for info, but they read
  // directly from the immutable AgentConfig object.

  @Override
  public String getAgentVersion() { return staticConfig.getAgentVersion(); }

  @Override
  public String getIngestionUrl() { return staticConfig.getIngestionUrl(); }

  @Override
  public int getMainQueueCapacity() {
    return staticConfig.getMainQueueCapacity();
  }

  @Override
  public int getHttpMaxConnectionsTotal() { return staticConfig.getHttpMaxConnectionsTotal(); }

  @Override
  public int getCircuitBreakerFailureThreshold() { return staticConfig.getCircuitBreakerFailureThreshold(); }

  @Override
  public String getLoggingFramework() {
    return staticConfig.getLoggingFramework();
  }

  @Override
  public int getHttpMaxConnectionsPerRoute() {
    return staticConfig.getHttpMaxConnectionsPerRoute();
  }

  @Override
  public int getHttpMaxRetryAttempts() {
    return staticConfig.getHttpMaxRetryAttempts();
  }

  @Override
  public long getCircuitBreakerOpenDurationMillis() {
    return staticConfig.getCircuitBreakerOpenDurationMillis();
  }

  @Override
  public boolean isKafkaAsyncSend() {
    return staticConfig.isKafkaAsyncSend();
  }

  @Override
  public String getKafkaCompressionType() {
    return staticConfig.getKafkaCompressionType();
  }

  @Override
  public void reloadConfig() {
    ConfigLoader.reloadConfig();
    LangaPrinter.printTrace("Configuration reloaded via JMX.");
  }
}