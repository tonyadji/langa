package com.capricedumardi.agent.core.config;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.helpers.EnvironmentUtils;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.config.jmx.AgentManagement;
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
        LangaPrinter.agentStarting();

        try {
            ConfigLoader.getConfigInstance();
            initSenderAndBuffers();
            LangaPrinter.printTrace("Buffers and sender initialized");
        } catch (Exception e) {
            LangaPrinter.printError("FATAL: Could not initialize sender/buffers. Agent disabled.");
            e.printStackTrace(System.err);
            return;
        }

        LoggingFramework framework = determineLoggingFramework();

        switch (framework) {
            case LOGBACK:
                LangaPrinter.printTrace("Using Logback for log collection");
                AppenderBinding.withLogBackAppender().bind();
                break;

            case LOG4J2:
                LangaPrinter.printTrace("Using Log4j2 for log collection");
                AppenderBinding.withLog4jAppender().bind();
                break;

            case NONE:
                LangaPrinter.printTrace("Log collection disabled (no logging framework configured)");
                LangaPrinter.printTrace("Metrics collection will still work");
                LangaPrinter.printTrace("To enable logs, set environment variable:");
                LangaPrinter.printTrace("LOGGING_FRAMEWORK=logback  (or log4j2)");
                break;
        }

        if (!isSpringPresent()) {
            LangaPrinter.printTrace("Spring NOT detected → using AspectJ weaver for metrics");
            initAspectJWeaver(agentArgs, inst);
        } else {
            LangaPrinter.printTrace("✓ Spring detected → expecting @EnableAspectJAutoProxy for metrics");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LangaPrinter.printTrace("Langa Agent: Shutdown initiated");
            BuffersFactory.shutdownAll();
            AppenderBinding.shutdown();
            LangaPrinter.printTrace("Langa Agent: Shutdown complete");
        }, "langa-agent-shutdown"));

        LangaPrinter.printTrace("========================================");
        LangaPrinter.printTrace("  Langa Agent Initialization Complete");
        LangaPrinter.printTrace("========================================");
    }

    private static LoggingFramework determineLoggingFramework() {
        String envFramework = ConfigLoader.getConfigInstance().getLoggingFramework();

        if (envFramework != null && !envFramework.trim().isEmpty()) {
            String framework = envFramework.trim().toLowerCase();

            switch (framework) {
                case "logback":
                    if (isClassPresent("ch.qos.logback.classic.LoggerContext")) {
                        LangaPrinter.printTrace("Using logging configured framework: Logback");
                        return LoggingFramework.LOGBACK;
                    } else {
                        LangaPrinter.printError("ERROR: LOGGING_FRAMEWORK=logback but Logback not found on classpath!");
                        LangaPrinter.printError("Falling back to classpath detection...");
                    }
                    break;

                case "log4j2", "log4j":
                    if (isClassPresent("org.apache.logging.log4j.core.LoggerContext")) {
                        LangaPrinter.printTrace("  Using logging configured framework: Log4j2");
                        return LoggingFramework.LOG4J2;
                    } else {
                        LangaPrinter.printError("ERROR: LOGGING_FRAMEWORK=log4j2 but Log4j2 not found on classpath!");
                        LangaPrinter.printError("Falling back to classpath detection...");
                    }
                    break;

                case "none","disabled":
                    LangaPrinter.printTrace("  Log collection explicitly disabled via LOGGING_FRAMEWORK=" + framework);
                    return LoggingFramework.NONE;

                default:
                    LangaPrinter.printError("WARNING: Unknown LOGGING_FRAMEWORK value: '" + envFramework + "'");
                    LangaPrinter.printError("Valid values: logback, log4j2, none");
                    LangaPrinter.printError("Falling back to classpath detection...");
            }
        }

        LangaPrinter.printTrace("LOGGING_FRAMEWORK not set, attempting classpath detection...");

        if (isClassPresent("ch.qos.logback.classic.LoggerContext") &&
                isClassPresent("org.slf4j.LoggerFactory")) {
            LangaPrinter.printTrace("Detected Logback on classpath");
            return LoggingFramework.LOGBACK;
        }

        if (isClassPresent("org.apache.logging.log4j.core.LoggerContext") &&
                isClassPresent("org.apache.logging.log4j.LogManager")) {
            LangaPrinter.printTrace("  Detected Log4j2 on classpath");
            return LoggingFramework.LOG4J2;
        }

        LangaPrinter.printTrace("  No supported logging framework found on classpath");
        return LoggingFramework.NONE;
    }

    private static void initSenderAndBuffers() {

      // Initialiser la couche de gestion dynamique (JMX)
      AgentManagement dynamicConfig = AgentManagement.getInstance();

      // 3. Préparer le Sender
      // Note: Tu devrais aussi passer dynamicConfig au SenderFactory si tu veux tuner le HTTP
      IngestionParamsResolver resolver = EnvironmentUtils.getIngestionParamsResolver();
      SenderService senderService = SenderServiceFactory.create(resolver, dynamicConfig);

      // 4. Initialiser les Buffers
      // IMPORTANT : On ne passe plus des int bruts, mais on laisse le factory
      // se connecter à la config dynamique.

      BuffersFactory.init(
          senderService,
          resolver.resolveAppKey(),
          resolver.resolveAccountKey(),
          dynamicConfig // On passe l'objet de management, pas juste les valeurs !
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
            LangaPrinter.printError("✗ Unable to initialize AspectJ weaver: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private enum LoggingFramework {
        LOGBACK,
        LOG4J2,
        NONE
    }
}
