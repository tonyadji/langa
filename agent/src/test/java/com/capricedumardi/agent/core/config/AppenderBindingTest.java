package com.capricedumardi.agent.core.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.capricedumardi.agent.core.appenders.AppenderType;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Binds real appenders to the actual logback/log4j2 root loggers, so every test here
 * detaches what it attached in @AfterEach to avoid leaking a live appender into the
 * shared root logger context for the rest of the test JVM.
 */
class AppenderBindingTest {

    @BeforeEach
    void ensureBuffersReady() {
        AgentTestSupport.ensureBuffersFactoryBootstrapped();
    }

    @AfterEach
    void detachBoundAppenders() {
        LoggerContext logbackContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logbackRoot = logbackContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logbackRoot.detachAppender(AppenderType.LANGA_LOGBACK_APPENDER.name());

        org.apache.logging.log4j.core.LoggerContext log4jContext =
                org.apache.logging.log4j.core.LoggerContext.getContext(false);
        Configuration config = log4jContext.getConfiguration();
        config.getRootLogger().removeAppender(AppenderType.LANGA_LOG4J_APPENDER.name());
        log4jContext.updateLoggers();
    }

    @Test
    void bindingLogbackAppenderRegistersItOnTheRootLogger() {
        AppenderBinding.bindLogBackAppender();

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        assertNotNull(root.getAppender(AppenderType.LANGA_LOGBACK_APPENDER.name()));
    }

    @Test
    void bindingLogbackAppenderTwiceDoesNotDuplicateIt() {
        AppenderBinding.bindLogBackAppender();
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        var first = root.getAppender(AppenderType.LANGA_LOGBACK_APPENDER.name());

        AppenderBinding.bindLogBackAppender();
        var second = root.getAppender(AppenderType.LANGA_LOGBACK_APPENDER.name());

        assertSame(first, second, "rebinding must be a no-op, not attach a second appender");
    }

    @Test
    void withLog4jAppenderBindsItToTheRootLogger() {
        AppenderBinding.withLog4jAppender().bind();

        Configuration config = org.apache.logging.log4j.core.LoggerContext.getContext(false).getConfiguration();
        assertNotNull(config.getAppender(AppenderType.LANGA_LOG4J_APPENDER.name()));
    }

    @Test
    void bindingLog4jAppenderTwiceDoesNotDuplicateIt() {
        AppenderBinding.withLog4jAppender().bind();
        Configuration config = org.apache.logging.log4j.core.LoggerContext.getContext(false).getConfiguration();
        var first = config.getAppender(AppenderType.LANGA_LOG4J_APPENDER.name());

        AppenderBinding.withLog4jAppender().bind();
        var second = config.getAppender(AppenderType.LANGA_LOG4J_APPENDER.name());

        assertSame(first, second, "rebinding must be a no-op, not attach a second appender");
    }

    @Test
    void factoryMethodsProduceUsableBindings() {
        assertDoesNotThrow(() -> AppenderBinding.withLogBackAppender().bind());
        assertDoesNotThrow(() -> AppenderBinding.withLog4jAppender().bind());
    }

    @Test
    void shutdownIsIdempotent() {
        AppenderBinding.withLogBackAppender().bind();

        assertDoesNotThrow(AppenderBinding::shutdown);
        assertDoesNotThrow(AppenderBinding::shutdown);
    }
}
