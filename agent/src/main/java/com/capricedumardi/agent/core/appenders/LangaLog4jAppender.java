package com.capricedumardi.agent.core.appenders;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Plugin(name = "LangaAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class LangaLog4jAppender extends AbstractAppender {
    private static final String AGENT_PACKAGE_PREFIX = "com.capricedumardi.agent";
    private static final Logger log = LogManager.getLogger(LangaLog4jAppender.class);
    private final GenericBuffer<LogEntry, SendableRequestDto> logBuffer;

    public LangaLog4jAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, null);
        try {
            this.logBuffer = BuffersFactory.getLogBufferInstance();
        } catch (Exception e) {
            LangaPrinter.printError("FATAL: LangaLog4jAppender failed to initialize: " + e.getMessage());
            e.printStackTrace(System.err);
            throw new IllegalStateException("Failed to initialize LangaLog4jAppender", e);
        }
    }


    @Override
    public void append(LogEvent event) {
        if (isAgentLog(event)) {
            return;
        }

        try {
            LogEntry entry = createLogEntry(event);
            log.trace("Adding entry to log buffer");
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
            LangaPrinter.printError("LangaLog4jAppender: Error flushing buffer during stop: " + e.getMessage());
        }
        super.stop();
    }

    private boolean isAgentLog(LogEvent event) {
        String loggerName = event.getLoggerName();
        return loggerName != null && loggerName.startsWith(AGENT_PACKAGE_PREFIX);
    }

    private LogEntry createLogEntry(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();

        // Use ISO-8601 timestamp for better observability backend compatibility
        String timestamp = Instant.ofEpochMilli(event.getTimeMillis()).toString();

        String threadName = event.getThreadName();

        String stackTrace = null;
        Throwable throwable = event.getThrown();
        if (throwable != null) {
            stackTrace = extractStackTrace(throwable);
        }

        Map<String, String> mdc = null;
        var contextData = event.getContextData();
        if (contextData != null && !contextData.isEmpty()) {
            mdc = new HashMap<>(contextData.toMap());
        }

        return new LogEntry(message, level, loggerName, timestamp, threadName, stackTrace, mdc);
    }

    private String extractStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        appendThrowable(sb, throwable, null);
        return sb.toString();
    }

    private void appendThrowable(StringBuilder sb, Throwable throwable, String prefix) {
        if (throwable == null) {
            return;
        }

        if (prefix != null) {
            sb.append(prefix);
        }

        sb.append(throwable.getClass().getName());

        String message = throwable.getMessage();
        if (message != null) {
            sb.append(": ").append(message);
        }
        sb.append("\n");

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        if (stackTrace != null) {
            for (StackTraceElement element : stackTrace) {
                sb.append("\tat ").append(element.toString()).append("\n");
            }
        }

        Throwable[] suppressed = throwable.getSuppressed();
        if (suppressed != null && suppressed.length > 0) {
            for (Throwable t : suppressed) {
                appendThrowable(sb, t, "\tSuppressed: ");
            }
        }

        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            appendThrowable(sb, cause, "Caused by: ");
        }
    }
}
