package com.capricedumardi.agent.core.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.capricedumardi.agent.core.appenders.AppenderType;
import com.capricedumardi.agent.core.appenders.LangaLog4jAppender;
import com.capricedumardi.agent.core.appenders.LangaLogbackAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.slf4j.LoggerFactory;

public class AppenderBinding {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(AppenderBinding.class);

    private final AppenderType selectedAppender;
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
            default -> log.warn("No appender selected.");
        }
    }

    public static void bindLogBackAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        if (rootLogger.getAppender(AppenderType.LANGA_LOGBACK_APPENDER.name()) != null) {
            log.info("Appender '{}' already bound to Logback root logger — skipping rebind.",
                    AppenderType.LANGA_LOGBACK_APPENDER.name());
            return;
        }

        LangaLogbackAppender langaAppender = new LangaLogbackAppender();
        langaAppender.setContext(loggerContext);
        langaAppender.setName(AppenderType.LANGA_LOGBACK_APPENDER.name());

        langaAppender.start();
        rootLogger.addAppender(langaAppender);

        log.info("Appender binding : LangaLogbackAppender bound successfully.");
    }

    private static void bindLog4jAppender() {
        org.apache.logging.log4j.core.LoggerContext ctx = org.apache.logging.log4j.core.LoggerContext.getContext(false);
        Configuration config = ctx.getConfiguration();
        final String appenderName = AppenderType.LANGA_LOG4J_APPENDER.name();

        if (config.getAppender(appenderName) != null) {
            log.debug("Appender '{}' already bound to Log4j2 root logger — skipping rebind.", appenderName);
            return;
        }

        PatternLayout layout = defaultPatternLayout();
        LangaLog4jAppender langaLog4jAppender = new LangaLog4jAppender(appenderName,null,layout);

        langaLog4jAppender.start();
        config.addAppender(langaLog4jAppender);

        LoggerConfig rootLoggerConfig = config.getRootLogger();
        rootLoggerConfig.addAppender(langaLog4jAppender, Level.ALL, null);

        ctx.updateLoggers();
        log.info("Appender binding : LangaLog4jAppender bound successfully.");
    }

    //TODO: make configurable (via a config loader class that could read several sources)
    private static PatternLayout defaultPatternLayout() {
        return PatternLayout.newBuilder()
                .withPattern("[%d{ISO8601}] [%t] %-5level %logger{36} - %msg%n")
                .build();
    }
}
