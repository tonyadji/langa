package com.capricedumardi.agent.core.appenders;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.capricedumardi.agent.testsupport.RecordingSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LangaLogbackAppenderTest {

    private LangaLogbackAppender appender;
    private RecordingSenderService sender;

    @BeforeEach
    void setUp() {
        AgentTestSupport.ensureBuffersFactoryBootstrapped();
        sender = AgentTestSupport.bootstrapSender();
        sender.reset();
        appender = new LangaLogbackAppender();
        appender.start();
    }

    private ILoggingEvent mockEvent(String loggerName, String message, Level level, IThrowableProxy throwableProxy) {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getLoggerName()).thenReturn(loggerName);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLevel()).thenReturn(level);
        when(event.getTimeStamp()).thenReturn(1700000000000L);
        when(event.getThreadName()).thenReturn("main");
        when(event.getThrowableProxy()).thenReturn(throwableProxy);
        when(event.getMDCPropertyMap()).thenReturn(Map.of());
        return event;
    }

    @Test
    void appendsNonAgentEventsToTheLogBuffer() {
        appender.append(mockEvent("com.myapp.Service", "hello", Level.INFO, null));
        BuffersFactory.getLogBufferInstance().flush();

        assertEquals(1, sender.sentPayloads().size());
        LogRequestDto dto = (LogRequestDto) sender.sentPayloads().get(0);
        assertEquals("hello", dto.entries().get(0).getMessage());
        assertEquals("INFO", dto.entries().get(0).getLevel());
    }

    @Test
    void filtersOutTheAgentsOwnInternalLogs() {
        appender.append(mockEvent("com.capricedumardi.agent.core.buffers.AbstractBuffer", "internal", Level.ERROR, null));
        BuffersFactory.getLogBufferInstance().flush();

        assertEquals(0, sender.sentPayloads().size());
    }

    @Test
    void extractsStackTraceWhenThrowableProxyIsPresent() {
        IThrowableProxy proxy = new ThrowableProxy(new RuntimeException("failure"));

        appender.append(mockEvent("com.myapp.Service", "boom", Level.ERROR, proxy));
        BuffersFactory.getLogBufferInstance().flush();

        LogRequestDto dto = (LogRequestDto) sender.sentPayloads().get(0);
        String stackTrace = dto.entries().get(0).getStackTrace();
        assertTrue(stackTrace.contains("RuntimeException"));
        assertTrue(stackTrace.contains("failure"));
    }

    @Test
    void extractsCausedByAndSuppressedChains() {
        RuntimeException suppressed = new RuntimeException("side issue");
        RuntimeException cause = new RuntimeException("root cause");
        RuntimeException top = new RuntimeException("top-level failure", cause);
        top.addSuppressed(suppressed);
        IThrowableProxy proxy = new ThrowableProxy(top);

        appender.append(mockEvent("com.myapp.Service", "chained", Level.ERROR, proxy));
        BuffersFactory.getLogBufferInstance().flush();

        LogRequestDto dto = (LogRequestDto) sender.sentPayloads().get(0);
        String stackTrace = dto.entries().get(0).getStackTrace();
        assertTrue(stackTrace.contains("top-level failure"));
        assertTrue(stackTrace.contains("Caused by"));
        assertTrue(stackTrace.contains("root cause"));
        assertTrue(stackTrace.contains("Suppressed"));
        assertTrue(stackTrace.contains("side issue"));
    }

    @Test
    void extractsMdcWhenPropertyMapIsPresent() {
        ILoggingEvent event = mockEvent("com.myapp.Service", "with-mdc", Level.INFO, null);
        when(event.getMDCPropertyMap()).thenReturn(Map.of("traceId", "abc-123"));

        appender.append(event);
        BuffersFactory.getLogBufferInstance().flush();

        LogRequestDto dto = (LogRequestDto) sender.sentPayloads().get(0);
        assertEquals("abc-123", dto.entries().get(0).getMdc().get("traceId"));
    }

    @Test
    void stopFlushesPendingEntries() {
        appender.append(mockEvent("com.myapp.Service", "flush-on-stop", Level.INFO, null));

        appender.stop();

        assertEquals(1, sender.sentPayloads().size());
    }
}
