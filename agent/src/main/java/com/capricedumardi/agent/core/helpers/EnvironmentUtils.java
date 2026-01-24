package com.capricedumardi.agent.core.helpers;

import com.capricedumardi.agent.core.config.AgentConfig;
import com.capricedumardi.agent.core.config.ConfigLoader;

public class EnvironmentUtils {

    private EnvironmentUtils() {
    }

    public static IngestionParamsResolver getIngestionParamsResolver() {
      AgentConfig agentConfig = ConfigLoader.getConfigInstance();
        return new IngestionParamsResolver(agentConfig.getIngestionUrl(), agentConfig.getSecret());
    }
}
