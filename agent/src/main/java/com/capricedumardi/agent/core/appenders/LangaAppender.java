package com.capricedumardi.agent.core.appenders;

import com.capricedumardi.agent.core.buffers.BuffersFactory;
import com.capricedumardi.agent.core.buffers.GenericBuffer;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.LogEntry;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.services.SenderService;
import com.capricedumardi.agent.core.services.SenderServiceFactory;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

@Plugin(name = "LangaAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class LangaAppender extends AbstractAppender {
    private static final Logger log = LogManager.getLogger(LangaAppender.class);
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final int DEFAULT_FLUSH_DELAY_IN_SECONDS = 5;
    private final GenericBuffer<LogEntry, SendableRequestDto> logBuffer;


    protected LangaAppender(String name, Filter filter, Layout<? extends Serializable> layout, String appKey, String accountKey, String appSecret) {
        super(name, filter, layout, true, null);
        SenderService senderService = SenderServiceFactory.createFromEnvironmentAndCredentialHelper(CredentialsHelper.of(appKey, accountKey, appSecret));
        BuffersFactory.init(senderService, appKey, accountKey, DEFAULT_BATCH_SIZE, DEFAULT_FLUSH_DELAY_IN_SECONDS);
        this.logBuffer = BuffersFactory.getLogBufferInstance();
    }

  @PluginFactory
    public static LangaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("appKey") String appKey,
            @PluginAttribute("accountKey") String accountKey,
            @PluginAttribute("appSecret") String appSecret) {
        return new LangaAppender(name, filter, layout, appKey, accountKey, appSecret);
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
                log.error("Error sending log to Langa: {}", e.getMessage(), e);
            }
    }

    @Override
    public void stop() {
        super.stop();
        logBuffer.shutdown();
    }
}
