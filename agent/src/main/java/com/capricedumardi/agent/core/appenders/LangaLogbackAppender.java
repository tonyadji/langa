package com.capricedumardi.agent.core.appenders;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LangaLogbackAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger log = LogManager.getLogger(LangaLogbackAppender.class);
    private GenericBuffer<LogEntry, SendableRequestDto> logBuffer;

    @Override
    public void start() {
        this.logBuffer = BuffersFactory.getLogBufferInstance();
        super.start();
        log.info("== LangaLogbackAppender started ==");
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            String message = eventObject.getFormattedMessage();
            String level = eventObject.getLevel().toString();
            String loggerName = eventObject.getLoggerName();
            String timestamp = String.valueOf(eventObject.getTimeStamp());

            LogEntry entry = new LogEntry(message, level, loggerName, timestamp);
            logBuffer.add(entry);

        } catch (Exception e) {
            addError("Error sending log to Langa", e);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (logBuffer != null) {
            logBuffer.shutdown();
        }
    }
}
