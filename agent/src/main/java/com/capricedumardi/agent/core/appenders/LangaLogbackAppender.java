package com.capricedumardi.agent.core.appenders;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.helpers.EnvironmentUtils;
import com.capricedumardi.agent.core.helpers.IngestionParamsResolver;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.services.SenderService;
import com.capricedumardi.agent.core.services.SenderServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LangaLogbackAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger log = LogManager.getLogger(LangaLogbackAppender.class);
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_FLUSH_DELAY_IN_SECONDS = 5;


    private GenericBuffer<LogEntry, SendableRequestDto> logBuffer;

    @Override
    public void start() {
        try {
            IngestionParamsResolver resolver = EnvironmentUtils.getIngestionParamsResolver();
            SenderService senderService = SenderServiceFactory.create(resolver);
            BuffersFactory.init(senderService, resolver.resolveAppKey(), resolver.resolveAccountKey(),
                    DEFAULT_BATCH_SIZE, DEFAULT_FLUSH_DELAY_IN_SECONDS);
            this.logBuffer = BuffersFactory.getLogBufferInstance();
            super.start();
            log.info("== LangaLogbackAppender started ==");
        } catch (Exception e) {
            addError("Failed to start LangaLogbackAppender", e);
        }
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            String message = eventObject.getFormattedMessage();
            String level = eventObject.getLevel().toString();
            String loggerName = eventObject.getLoggerName();

            String timestamp = Instant.ofEpochMilli(eventObject.getTimeStamp())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

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
