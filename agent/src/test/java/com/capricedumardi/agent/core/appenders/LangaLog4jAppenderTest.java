package com.capricedumardi.agent.core.appenders;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.model.LogRequestDto;
import com.capricedumardi.agent.testsupport.AgentTestSupport;
import com.capricedumardi.agent.testsupport.RecordingSenderService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LangaLog4jAppenderTest {

    private LangaLog4jAppender appender;
    private RecordingSenderService sender;

    @BeforeEach
    void setUp() {
        AgentTestSupport.ensureBuffersFactoryBootstrapped();
        sender = AgentTestSupport.bootstrapSender();
        sender.reset();
        appender = new LangaLog4jAppender("test-appender", null, null);
    }

    private LogEvent mockEvent(String loggerName, String message, Level level, Throwable thrown) {
        LogEvent event = mock(LogEvent.class);
        when(event.getLoggerName()).thenReturn(loggerName);
        when(event.getMessage()).thenReturn(new SimpleMessage(message));
        when(event.getLevel()).thenReturn(level);
        when(event.getTimeMillis()).thenReturn(1700000000000L);
        when(event.getThreadName()).thenReturn("main");
        when(event.getThrown()).thenReturn(thrown);
        when(event.getContextData()).thenReturn(null);
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
    void extractsStackTraceWhenThrowableIsPresent() {
        appender.append(mockEvent("com.myapp.Service", "boom", Level.ERROR, new RuntimeException("failure")));
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

        appender.append(mockEvent("com.myapp.Service", "chained", Level.ERROR, top));
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
    void extractsMdcWhenContextDataIsPresent() {
        SortedArrayStringMap contextData = new SortedArrayStringMap();
        contextData.putValue("traceId", "abc-123");

        LogEvent event = mockEvent("com.myapp.Service", "with-mdc", Level.INFO, null);
        when(event.getContextData()).thenReturn(contextData);

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
