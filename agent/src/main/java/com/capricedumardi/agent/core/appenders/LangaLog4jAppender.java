package com.capricedumardi.agent.core.appenders;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;

import java.io.Serializable;

@Plugin(name = "LangaAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class LangaLog4jAppender extends AbstractAppender {
    private static final Logger log = LogManager.getLogger(LangaLog4jAppender.class);
    private final GenericBuffer<LogEntry, SendableRequestDto> logBuffer;

    public LangaLog4jAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, null);
        this.logBuffer = BuffersFactory.getLogBufferInstance();
    }


    @Override
    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();
        String timestamp = String.valueOf(event.getTimeMillis());

        try {
            LogEntry entry = new LogEntry(message, level, loggerName, timestamp);
            log.trace("Adding entry to log buffer");
            logBuffer.add(entry);

        } catch (Exception e) {
            log.error("Error sending log to Langa: {}", e.getMessage(), e);
        }
    }
}
