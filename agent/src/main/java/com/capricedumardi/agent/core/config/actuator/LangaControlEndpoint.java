package com.capricedumardi.agent.core.config.actuator;

import com.capricedumardi.agent.core.config.jmx.AgentDynamicConfig;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@Endpoint(id = "langaConfig")
public class LangaControlEndpoint {

  private final AgentDynamicConfig dynamicConfig = AgentDynamicConfig.getInstance();

  @ReadOperation
  public Map<String, Object> getConfig() {
    Map<String, Object> config = new HashMap<>();

    // Dynamic (RW)
    config.put("batchSize", dynamicConfig.getBufferBatchSize());
    config.put("flushIntervalSeconds", dynamicConfig.getBufferFlushIntervalSeconds());
    config.put("debugMode", dynamicConfig.isDebugMode());
    config.put("compressionThreshold", dynamicConfig.getHttpCompressionThresholdBytes());

    // Static (RO)
    config.put("agentVersion", dynamicConfig.getAgentVersion());
    config.put("ingestionUrl", dynamicConfig.getIngestionUrl());
    config.put("loggingFramework", dynamicConfig.getLoggingFramework());

    return config;
  }

  @WriteOperation
  public void updateConfig(@Nullable Integer batchSize,
      @Nullable Integer flushIntervalSeconds,
      @Nullable Boolean debugMode,
      @Nullable Integer compressionThreshold) {

    if (batchSize != null) dynamicConfig.setBufferBatchSize(batchSize);
    if (flushIntervalSeconds != null) {
      int flushInterval = flushIntervalSeconds;
      if (flushInterval > 0) {
        dynamicConfig.setBufferFlushIntervalSeconds(flushIntervalSeconds);
      }
    }

    if (debugMode != null) dynamicConfig.setDebugMode(debugMode);
    if (compressionThreshold != null) {
      int threshold = compressionThreshold;
      if (threshold > 0) {
        dynamicConfig.setHttpCompressionThresholdBytes(threshold);
      }
    }
  }
}