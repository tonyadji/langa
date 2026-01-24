package com.capricedumardi.agent.core.config.actuator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;

@Configuration
@ConditionalOnClass(Endpoint.class)
public class LangaActuatorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean // Allows the user to override if needed
  public LangaMetricsEndpoint langaMetricsEndpoint() {
    return new LangaMetricsEndpoint();
  }

  @Bean
  @ConditionalOnMissingBean
  public LangaControlEndpoint langaControlEndpoint() {
    return new LangaControlEndpoint();
  }
}