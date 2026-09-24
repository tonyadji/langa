package com.capricedumardi.agent.core.config.actuator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class LangaActuatorAutoConfigurationTest {

    @Test
    void beanMethodsProduceUsableEndpoints() {
        LangaActuatorAutoConfiguration config = new LangaActuatorAutoConfiguration();

        assertNotNull(config.langaMetricsEndpoint());
        assertNotNull(config.langaControlEndpoint());
    }
}
