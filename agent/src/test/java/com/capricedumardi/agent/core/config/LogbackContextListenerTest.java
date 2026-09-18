package com.capricedumardi.agent.core.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogbackContextListenerTest {

    @Test
    void isResetResistantIsTrue() {
        LogbackContextListener listener = new LogbackContextListener(ctx -> { });
        assertTrue(listener.isResetResistant());
    }

    @Test
    void onStartInvokesConsumer() {
        AtomicInteger calls = new AtomicInteger();
        LogbackContextListener listener = new LogbackContextListener(ctx -> calls.incrementAndGet());

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        listener.onStart(context);

        assertEquals(1, calls.get());
    }

    @Test
    void onResetInvokesConsumer() {
        AtomicInteger calls = new AtomicInteger();
        LogbackContextListener listener = new LogbackContextListener(ctx -> calls.incrementAndGet());

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        listener.onReset(context);

        assertEquals(1, calls.get());
    }

    @Test
    void onLevelChangeInvokesConsumerWithLoggerContext() {
        AtomicInteger calls = new AtomicInteger();
        LogbackContextListener listener = new LogbackContextListener(ctx -> calls.incrementAndGet());

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger("com.capricedumardi.test.levelchange");

        listener.onLevelChange(logger, Level.DEBUG);

        assertEquals(1, calls.get());
    }

    @Test
    void onStopDoesNotThrow() {
        LogbackContextListener listener = new LogbackContextListener(ctx -> { });
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        listener.onStop(context);
    }
}
