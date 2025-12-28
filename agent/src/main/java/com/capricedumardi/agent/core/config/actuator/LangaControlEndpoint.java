package com.capricedumardi.agent.core.config.actuator;

import com.capricedumardi.agent.core.config.jmx.AgentManagement;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@Endpoint(id = "langaConfig")
public class LangaControlEndpoint {

  private final AgentManagement management = AgentManagement.getInstance();

  @ReadOperation
  public Map<String, Object> getConfig() {
    Map<String, Object> config = new HashMap<>();

    // Dynamic (RW)
    config.put("batchSize", management.getBufferBatchSize());
    config.put("flushIntervalSeconds", management.getBufferFlushIntervalSeconds());
    config.put("debugMode", management.isDebugMode());
    config.put("compressionThreshold", management.getHttpCompressionThresholdBytes());

    // Static (RO)
    config.put("agentVersion", management.getAgentVersion());
    config.put("ingestionUrl", management.getIngestionUrl());
    config.put("loggingFramework", management.getLoggingFramework());

    return config;
  }

  @WriteOperation
  public void updateConfig(@Nullable Integer batchSize,
      @Nullable Integer flushIntervalSeconds,
      @Nullable Boolean debugMode,
      @Nullable Integer compressionThreshold) {

    if (batchSize != null) management.setBufferBatchSize(batchSize);
    if (flushIntervalSeconds != null) management.setBufferFlushIntervalSeconds(flushIntervalSeconds);
    if (debugMode != null) management.setDebugMode(debugMode);
    if (compressionThreshold != null) management.setHttpCompressionThresholdBytes(compressionThreshold);
  }
}