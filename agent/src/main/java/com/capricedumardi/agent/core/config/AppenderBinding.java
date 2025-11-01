package com.capricedumardi.agent.core.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.capricedumardi.agent.core.appenders.LangaLog4jAppender;
import com.capricedumardi.agent.core.appenders.LangaLogbackAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.slf4j.LoggerFactory;

public class AppenderBinding {
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(AppenderBinding.class);

    private AppenderBinding(String selectedAppender){
        this.selectedAppender = selectedAppender;
    }

    private static final String LOGBACK_APPENDER_NAME = "LangaLogbackAppender";
    private static final String LOG4J_APPENDER_NAME = "LangaLog4jAppender";
    private final String selectedAppender;

    public static AppenderBinding withLogBackAppender() {
        return new AppenderBinding(LOGBACK_APPENDER_NAME);
    }

    public static AppenderBinding withLog4jAppender() {
        return new AppenderBinding(LOG4J_APPENDER_NAME);
    }

    public void bind() {
        switch (selectedAppender) {
            case LOG4J_APPENDER_NAME -> bindLog4jAppender();
            case LOGBACK_APPENDER_NAME -> bindLogBackAppender();
            default -> log.warn("No appender selected.");
        }
    }

    public static void bindLogBackAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        LangaLogbackAppender langaAppender = new LangaLogbackAppender();
        langaAppender.setContext(loggerContext);
        langaAppender.setName(LOGBACK_APPENDER_NAME);

        langaAppender.start();
        Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(langaAppender);

        log.info("Appender binding : LangaLogbackAppender bound successfully.");
    }

    private static void bindLog4jAppender() {
        org.apache.logging.log4j.core.LoggerContext ctx = org.apache.logging.log4j.core.LoggerContext.getContext(false);
        Configuration config = ctx.getConfiguration();

        LangaLog4jAppender langaLog4jAppender = new LangaLog4jAppender(LOG4J_APPENDER_NAME,null,null);

        langaLog4jAppender.start();
        config.addAppender(langaLog4jAppender);

        LoggerConfig rootLoggerConfig = config.getRootLogger();
        rootLoggerConfig.addAppender(langaLog4jAppender, Level.ALL, null);

        ctx.updateLoggers();
        log.info("Appender binding : LangaLog4jAppender bound successfully.");
    }
}
