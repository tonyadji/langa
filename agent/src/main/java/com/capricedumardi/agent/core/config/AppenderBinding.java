package com.capricedumardi.agent.core.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.capricedumardi.agent.core.appenders.AppenderType;
import com.capricedumardi.agent.core.appenders.LangaLog4jAppender;
import com.capricedumardi.agent.core.appenders.LangaLogbackAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppenderBinding {
    private final AppenderType selectedAppender;
    private static final AtomicBoolean watcher = new AtomicBoolean(false);
    private static ScheduledExecutorService watchScheduler;

    private AppenderBinding(AppenderType selectedAppender){
        this.selectedAppender = selectedAppender;
    }


    public static AppenderBinding withLogBackAppender() {
        return new AppenderBinding(AppenderType.LANGA_LOGBACK_APPENDER);
    }

    public static AppenderBinding withLog4jAppender() {
        return new AppenderBinding(AppenderType.LANGA_LOG4J_APPENDER);
    }

    public void bind() {
        switch (selectedAppender) {
            case LANGA_LOG4J_APPENDER -> bindLog4jAppender();
            case LANGA_LOGBACK_APPENDER -> bindLogBackAppender();
            default -> LangaPrinter.printError("No appender selected.");
        }
        startWatch();
    }

    public static void bindLogBackAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        final LogbackContextListener listener = new LogbackContextListener(AppenderBinding::bindLangaLogbackAppender);
        loggerContext.addListener(listener);
        bindLangaLogbackAppender(loggerContext);
    }

    public static void shutdown() {
        if (watchScheduler != null && !watchScheduler.isShutdown()) {
            LangaPrinter.printTrace("Shutting down watcher...");
            watchScheduler.shutdownNow();
        }
    }

    private static void bindLog4jAppender() {
        org.apache.logging.log4j.core.LoggerContext ctx = org.apache.logging.log4j.core.LoggerContext.getContext(false);
        Configuration config = ctx.getConfiguration();
        final String appenderName = AppenderType.LANGA_LOG4J_APPENDER.name();

        if (config.getAppender(appenderName) != null) {
            LangaPrinter.printTrace("Appender"+appenderName+" already bound to Log4j2 root logger — skipping rebind.");
            return;
        }

        PatternLayout layout = defaultPatternLayout();
        LangaLog4jAppender langaLog4jAppender = new LangaLog4jAppender(appenderName,null,layout);

        langaLog4jAppender.start();
        config.addAppender(langaLog4jAppender);

        LoggerConfig rootLoggerConfig = config.getRootLogger();
        rootLoggerConfig.addAppender(langaLog4jAppender, Level.ALL, null);

        ctx.updateLoggers();
        LangaPrinter.printTrace("Appender binding : LangaLog4jAppender bound successfully.");
    }

    //TODO: make configurable (via a config loader class that could read several sources)
    private static PatternLayout defaultPatternLayout() {
        return PatternLayout.newBuilder()
                .withPattern("[%d{ISO8601}] [%t] %-5level %logger{36} - %msg%n")
                .build();
    }

    private static void bindLangaLogbackAppender(LoggerContext loggerContext) {
        Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if (rootLogger.getAppender(AppenderType.LANGA_LOGBACK_APPENDER.name()) != null) {
            LangaPrinter.printTrace("Appender "+AppenderType.LANGA_LOGBACK_APPENDER.name()+" already bound to Logback root logger — skipping rebind.");
            return;
        }

        LangaLogbackAppender langaAppender = new LangaLogbackAppender();
        langaAppender.setContext(loggerContext);
        langaAppender.setName(AppenderType.LANGA_LOGBACK_APPENDER.name());

        langaAppender.start();
        rootLogger.addAppender(langaAppender);

        LangaPrinter.printTrace("Appender binding : LangaLogbackAppender bound successfully.");
    }

    private void startWatch() {
        LangaPrinter.printTrace("Starting watcher...");
        if (watcher.getAndSet(true)) {
            return;
        }

        watchScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Langa-Appender-Watcher");
            t.setDaemon(true);
            return t;
        });

        watchScheduler.scheduleAtFixedRate(() -> {
            try {
                if (selectedAppender == AppenderType.LANGA_LOGBACK_APPENDER) {
                    ensureLogbackBinding();
                } else if (selectedAppender == AppenderType.LANGA_LOG4J_APPENDER) {
                    // relevant code for Log4j can be added here
                }
            } catch (Exception e) {
                LangaPrinter.printConditionalError("Error while binding appender: " + e.getMessage());
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    private static void ensureLogbackBinding() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if (rootLogger.getAppender(AppenderType.LANGA_LOGBACK_APPENDER.name()) == null) {
            LangaPrinter.printTrace("[Langa AppenderBinding] Appender detached. Re-binding to current context...");
            bindLogBackAppender();
        }
    }
}
