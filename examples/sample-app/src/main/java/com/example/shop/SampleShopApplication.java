package com.example.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * A tiny shop instrumented with the Langa agent: started with {@code -javaagent:langa-agent.jar}, its logs are
 * shipped by the agent appender and its {@code @Monitored} methods are timed by the agent (AspectJ load-time weaving).
 */
@SpringBootApplication
@EnableScheduling
public class SampleShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleShopApplication.class, args);
    }
}
