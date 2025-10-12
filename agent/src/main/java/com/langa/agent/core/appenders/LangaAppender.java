package com.langa.agent.core.appenders;

import com.langa.agent.core.buffers.BuffersFactory;
import com.langa.agent.core.buffers.GenericBuffer;
import com.langa.agent.core.helpers.CredentialsHelper;
import com.langa.agent.core.model.LogEntry;
import com.langa.agent.core.model.SendableRequestDto;
import com.langa.agent.core.services.HttpSenderService;
import com.langa.agent.core.services.SenderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;

@Plugin(name = "LangaAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class LangaAppender extends AbstractAppender {
    private static final Logger log = LogManager.getLogger(LangaAppender.class);
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_FLUSH_DELAY_IN_SECONDS = 5;
    private final GenericBuffer<LogEntry, SendableRequestDto> logBuffer;


    protected LangaAppender(String name, Filter filter, Layout<? extends Serializable> layout, String url, String appKey, String accountKey, String appSecret) {
        super(name, filter, layout, true, null);
        SenderService senderService = new HttpSenderService(url, CredentialsHelper.of(appKey, accountKey, appSecret).getCredentials(CredentialsHelper.CredentialType.HTTP));
        BuffersFactory.init(senderService, appKey, accountKey, DEFAULT_BATCH_SIZE, DEFAULT_FLUSH_DELAY_IN_SECONDS);
        this.logBuffer = BuffersFactory.getLogBufferInstance();
    }

    @PluginFactory
    public static LangaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("url") String url,
            @PluginAttribute("appKey") String appKey,
            @PluginAttribute("accountKey") String accountKey,
            @PluginAttribute("accountKey") String appSecret) {
        return new LangaAppender(name, filter, layout, url, appKey, accountKey, appSecret);
    }

    @Override
    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();
        String timestamp = (event.getTimeMillis() > 0 ?
                new java.util.Date(event.getTimeMillis()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            try {
                LogEntry entry = new LogEntry(
                        message,
                        level,
                        loggerName,
                        timestamp
                );

                log.debug("Adding entry to log buffer");
                logBuffer.add(entry);

            } catch (Exception e) {
                log.error("Error sending log to Langa: %s", e.getMessage());
            }
    }

    @Override
    public void stop() {
        super.stop();
        logBuffer.shutdown();
    }
}
