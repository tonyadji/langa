package com.capricedumardi.agent.core.config;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.helpers.EnvironmentUtils;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.services.SenderService;
import com.capricedumardi.agent.core.services.SenderServiceFactory;
import org.aspectj.weaver.loadtime.Agent;

import java.lang.instrument.Instrumentation;

public class LangaAgentInitializer {

    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_FLUSH_DELAY_IN_SECONDS = 5;
    private static final String LOGGING_FRAMEWORK = "LOGGING_FRAMEWORK";

    private LangaAgentInitializer() {}

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("========================================");
        System.out.println("  Langa Agent Starting");
        System.out.println("========================================");

        try {
            initSenderAndBuffers();
            System.out.println("Buffers and sender initialized");
        } catch (Exception e) {
            System.err.println("FATAL: Could not initialize sender/buffers. Agent disabled.");
            e.printStackTrace(System.err);
            return;
        }

        LoggingFramework framework = determineLoggingFramework();

        switch (framework) {
            case LOGBACK:
                System.out.println("Using Logback for log collection");
                AppenderBinding.withLogBackAppender().bind();
                break;

            case LOG4J2:
                System.out.println("Using Log4j2 for log collection");
                AppenderBinding.withLog4jAppender().bind();
                break;

            case NONE:
                System.out.println("Log collection disabled (no logging framework configured)");
                System.out.println("  Metrics collection will still work");
                System.out.println("  To enable logs, set environment variable:");
                System.out.println("    LOGGING_FRAMEWORK=logback  (or log4j2)");
                break;
        }

        if (!isSpringPresent()) {
            System.out.println("Spring NOT detected → using AspectJ weaver for metrics");
            initAspectJWeaver(agentArgs, inst);
        } else {
            System.out.println("✓ Spring detected → expecting @EnableAspectJAutoProxy for metrics");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Langa Agent: Shutdown initiated");
            BuffersFactory.shutdownAll();
            System.out.println("Langa Agent: Shutdown complete");
        }, "langa-agent-shutdown"));

        System.out.println("========================================");
        System.out.println("  Langa Agent Initialization Complete");
        System.out.println("========================================");
    }

    private static LoggingFramework determineLoggingFramework() {
        String envFramework = System.getenv(LOGGING_FRAMEWORK);

        if (envFramework != null && !envFramework.trim().isEmpty()) {
            String framework = envFramework.trim().toLowerCase();

            switch (framework) {
                case "logback":
                    if (isClassPresent("ch.qos.logback.classic.LoggerContext")) {
                        System.out.println("Using logging configured framework: Logback");
                        return LoggingFramework.LOGBACK;
                    } else {
                        System.err.println("ERROR: LOGGING_FRAMEWORK=logback but Logback not found on classpath!");
                        System.err.println("Falling back to classpath detection...");
                    }
                    break;

                case "log4j2", "log4j":
                    if (isClassPresent("org.apache.logging.log4j.core.LoggerContext")) {
                        System.out.println("  Using logging configured framework: Log4j2");
                        return LoggingFramework.LOG4J2;
                    } else {
                        System.err.println("ERROR: LOGGING_FRAMEWORK=log4j2 but Log4j2 not found on classpath!");
                        System.err.println("Falling back to classpath detection...");
                    }
                    break;

                case "none","disabled":
                    System.out.println("  Log collection explicitly disabled via LOGGING_FRAMEWORK=" + framework);
                    return LoggingFramework.NONE;

                default:
                    System.err.println("WARNING: Unknown LOGGING_FRAMEWORK value: '" + envFramework + "'");
                    System.err.println("Valid values: logback, log4j2, none");
                    System.err.println("Falling back to classpath detection...");
            }
        }

        System.out.println("LOGGING_FRAMEWORK not set, attempting classpath detection...");

        if (isClassPresent("ch.qos.logback.classic.LoggerContext") &&
                isClassPresent("org.slf4j.LoggerFactory")) {
            System.out.println("Detected Logback on classpath");
            return LoggingFramework.LOGBACK;
        }

        if (isClassPresent("org.apache.logging.log4j.core.LoggerContext") &&
                isClassPresent("org.apache.logging.log4j.LogManager")) {
            System.out.println("  Detected Log4j2 on classpath");
            return LoggingFramework.LOG4J2;
        }

        System.out.println("  No supported logging framework found on classpath");
        return LoggingFramework.NONE;
    }

    private static void initSenderAndBuffers() {
        IngestionParamsResolver resolver = EnvironmentUtils.getIngestionParamsResolver();
        SenderService senderService = SenderServiceFactory.create(resolver);

        int batchSize = getConfigInt("LANGA_BATCH_SIZE", DEFAULT_BATCH_SIZE);
        int flushInterval = getConfigInt("LANGA_FLUSH_INTERVAL_SECONDS", DEFAULT_FLUSH_DELAY_IN_SECONDS);

        BuffersFactory.init(
                senderService,
                resolver.resolveAppKey(),
                resolver.resolveAccountKey(),
                batchSize,
                flushInterval
        );
    }

    private static boolean isSpringPresent() {
        return isClassPresent("org.springframework.aop.framework.ProxyFactory");
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void initAspectJWeaver(String agentArgs, Instrumentation inst) {
        try {
            Agent.premain(agentArgs, inst);
        } catch (Exception e) {
            System.err.println("✗ Unable to initialize AspectJ weaver: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static int getConfigInt(String envKey, int defaultValue) {
        String envVar = System.getenv(envKey);
        if (envVar != null && !envVar.trim().isEmpty()) {
            try {
                return Integer.parseInt(envVar.trim());
            } catch (NumberFormatException e) {
                System.err.println("Invalid value for " + envKey + ": " + envVar +
                        " (using default: " + defaultValue + ")");
            }
        }
        return defaultValue;
    }

    private enum LoggingFramework {
        LOGBACK,
        LOG4J2,
        NONE
    }
}
