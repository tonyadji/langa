package com.capricedumardi.agent.core.appenders;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;
import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class LangaLogbackAppender extends AppenderBase<ILoggingEvent> {
    private static final String AGENT_PACKAGE_PREFIX = "com.capricedumardi.agent";
    private static final Logger log = LogManager.getLogger(LangaLogbackAppender.class);
    private GenericBuffer<LogEntry, SendableRequestDto> logBuffer;

    @Override
    public void start() {
        this.logBuffer = BuffersFactory.getLogBufferInstance();
        super.start();
        log.info("== LangaLogbackAppender started ==");
    }

    @Override
    protected void append(ILoggingEvent loggingEvent) {
        if (isAgentLog(loggingEvent)) {
            return;
        }

        try {
            LogEntry entry = createLogEntry(loggingEvent);
            logBuffer.add(entry);

        } catch (Exception e) {
            log.error("Error sending log to Langa: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            if (logBuffer != null) {
                logBuffer.flush();
            }
        } catch (Exception e) {
            System.err.println("LangaLog4jAppender: Error flushing buffer during stop: " + e.getMessage());
        }
        super.stop();
    }

    private LogEntry createLogEntry(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();

        // Use ISO-8601 timestamp for better observability backend compatibility
        String timestamp = Instant.ofEpochMilli(event.getTimeStamp()).toString();

        String threadName = event.getThreadName();

        String stackTrace = null;
        IThrowableProxy throwableProxy = event.getThrowableProxy();
        if (throwableProxy != null) {
            stackTrace = extractStackTrace(throwableProxy);
        }

        Map<String, String> mdc = null;
        Map<String, String> mdcPropertyMap = event.getMDCPropertyMap();
        if (mdcPropertyMap != null && !mdcPropertyMap.isEmpty()) {
            mdc = new HashMap<>(mdcPropertyMap);
        }

        return new LogEntry(message, level, loggerName, timestamp, threadName, stackTrace, mdc);
    }

    private boolean isAgentLog(ILoggingEvent  event) {
        String loggerName = event.getLoggerName();
        return loggerName != null && loggerName.startsWith(AGENT_PACKAGE_PREFIX);
    }

    private String extractStackTrace(IThrowableProxy throwableProxy) {
        if (throwableProxy == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        appendThrowableProxy(sb, throwableProxy, null);
        return sb.toString();
    }

    private void appendThrowableProxy(StringBuilder sb, IThrowableProxy proxy, String prefix) {
        if (proxy == null) {
            return;
        }

        if (prefix != null) {
            sb.append(prefix);
        }

        sb.append(proxy.getClassName());

        String message = proxy.getMessage();
        if (message != null) {
            sb.append(": ").append(message);
        }
        sb.append("\n");

        StackTraceElementProxy[] stackTrace = proxy.getStackTraceElementProxyArray();
        if (stackTrace != null) {
            for (StackTraceElementProxy element : stackTrace) {
                sb.append("\tat ").append(element.getStackTraceElement().toString()).append("\n");
            }
        }

        IThrowableProxy[] suppressed = proxy.getSuppressed();
        if (suppressed != null && suppressed.length > 0) {
            for (IThrowableProxy t : suppressed) {
                appendThrowableProxy(sb, t, "\tSuppressed: ");
            }
        }

        IThrowableProxy cause = proxy.getCause();
        if (cause != null && cause != proxy) {
            int commonFrames = proxy.getCommonFrames();
            if (commonFrames > 0) {
                sb.append("\t... ").append(commonFrames).append(" more\n");
            }
            appendThrowableProxy(sb, cause, "Caused by: ");
        }
    }
}
