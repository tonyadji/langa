package com.capricedumardi.agent.core.appenders;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.services.SenderService;
import com.capricedumardi.agent.core.services.SenderServiceFactory;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LangaLogbackAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger log = LogManager.getLogger(LangaLogbackAppender.class);
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_FLUSH_DELAY_IN_SECONDS = 5;

    private String appKey;
    private String accountKey;
    private String appSecret;

    private GenericBuffer<LogEntry, SendableRequestDto> logBuffer;

    @Override
    public void start() {
        try {
            SenderService senderService = SenderServiceFactory.createFromEnvironmentAndCredentialHelper(CredentialsHelper.of(appKey, accountKey, appSecret));
            BuffersFactory.init(senderService, appKey, accountKey,
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

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public void setAppSecret(String appSecret) {this.appSecret = appSecret;}

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }
}
