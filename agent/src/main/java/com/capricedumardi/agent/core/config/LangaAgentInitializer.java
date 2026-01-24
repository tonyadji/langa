package com.capricedumardi.agent.core.config;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.config.jmx.AgentDynamicConfig;
import com.capricedumardi.agent.core.helpers.EnvironmentUtils;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.services.SenderService;
import com.capricedumardi.agent.core.services.SenderServiceFactory;
import org.aspectj.weaver.loadtime.Agent;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

public class LangaAgentInitializer {

    private static ClassLoader AGENT_CLASSLOADER;

    private LangaAgentInitializer() {}

    public static void premain(String agentArgs, Instrumentation inst) {
        AGENT_CLASSLOADER = LangaAgentInitializer.class.getClassLoader();
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

        if (!isSpringPresent(inst)) {
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
        LoggingFramework selectedFramework = LoggingFramework.NONE;
        if (envFramework != null && !envFramework.trim().isEmpty()) {
            String framework = envFramework.trim().toLowerCase();

            selectedFramework = switch (framework) {
                case "logback":
                    if (isClassPresent("ch.qos.logback.classic.LoggerContext")) {
                        LangaPrinter.printTrace("Using logging configured framework: Logback");
                        yield LoggingFramework.LOGBACK;
                    } else {
                        LangaPrinter.printError("ERROR: LOGGING_FRAMEWORK=logback but Logback not found on classpath!");
                        LangaPrinter.printError("Falling back to classpath detection...");
                        yield LoggingFramework.NONE;
                    }

                case "log4j2", "log4j":
                    if (isClassPresent("org.apache.logging.log4j.core.LoggerContext")) {
                        LangaPrinter.printTrace("  Using logging configured framework: Log4j2");
                        yield LoggingFramework.LOG4J2;
                    } else {
                        LangaPrinter.printError("ERROR: LOGGING_FRAMEWORK=log4j2 but Log4j2 not found on classpath!");
                        LangaPrinter.printError("Falling back to classpath detection...");
                        yield LoggingFramework.NONE;
                    }

                case "none","disabled":
                    LangaPrinter.printTrace(" No LOGGING_FRAMEWORK found in environment variables or file configuration");
                    LangaPrinter.printError("Falling back to classpath detection...");
                    yield LoggingFramework.NONE;

                default:
                    LangaPrinter.printError("WARNING: Unknown LOGGING_FRAMEWORK value: '" + envFramework + "'");
                    LangaPrinter.printError("Valid values: logback, log4j2, none");
                    LangaPrinter.printError("Falling back to classpath detection...");
                    yield LoggingFramework.NONE;
            };
        }

        if (!Objects.equals(selectedFramework, LoggingFramework.NONE)) {
            return selectedFramework;
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

      // Initialize the dynamic management layer (JMX)
      AgentDynamicConfig dynamicConfig = AgentDynamicConfig.getInstance();

      // 3. Prepare the Sender
      // Note: You should also pass dynamicConfig to SenderFactory if you want to tune HTTP
      IngestionParamsResolver resolver = EnvironmentUtils.getIngestionParamsResolver();
      SenderService senderService = SenderServiceFactory.create(resolver, dynamicConfig);

      // 4. Initialize the Buffers
      // IMPORTANT: We no longer pass raw ints, but let the factory
      // connect to the dynamic config.

      BuffersFactory.init(
          senderService,
          resolver.resolveAppKey(),
          resolver.resolveAccountKey(),
          dynamicConfig // We pass the management object, not just the values!
      );
    }

    private static boolean isSpringPresent(Instrumentation inst) {
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            if (!clazz.getName().startsWith("org.springframework")) {
                continue;
            }

            ClassLoader cl = clazz.getClassLoader();

            if (cl == null) {
                continue;
            }

            if (cl == AGENT_CLASSLOADER) {
                continue;
            }
            return true;
        }
        return false;
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
