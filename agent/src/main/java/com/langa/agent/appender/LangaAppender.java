package com.langa.agent.appender;

import com.google.gson.Gson;
import com.langa.agent.model.LogEntry;
import com.langa.agent.model.LogRequestDto;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(name = "LangaAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
public class LangaAppender extends AbstractAppender {
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final Gson gson = new Gson();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private final String url;
    private final String appKey;
    private final String accountKey;

    protected LangaAppender(String name, Filter filter, Layout<? extends Serializable> layout, String url, String appKey, String accountKey) {
        super(name, filter, layout);
        this.url = url;
        this.appKey = appKey;
        this.accountKey = accountKey;
    }

    @PluginFactory
    public static LangaAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginAttribute("url") String url,
            @PluginAttribute("appKey") String appKey,
            @PluginAttribute("accountKey") String accountKey) {
        return new LangaAppender(name, filter, layout, url, appKey, accountKey);
    }

    @Override
    public void append(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();
        String timestamp = (event.getTimeMillis() > 0 ?
                new java.util.Date(event.getTimeMillis()).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        executorService.execute(() -> {
            try {
                LogEntry logData = new LogEntry(
                        message,
                        level,
                        loggerName,
                        timestamp
                );

                LogRequestDto requestDto = new LogRequestDto(this.appKey, this.accountKey, Collections.singletonList(logData));

                String requestBody = gson.toJson(requestDto);

                HttpPost httpPost = new HttpPost(url);
                StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
                httpPost.setEntity(entity);

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode >= 400) {
                        System.err.println("LangaAppender - Failed with status " + statusCode);
                    }
                }

            } catch (Exception e) {
                System.err.println("Error sending log to Langa: " + e.getMessage());
            }
        });
    }

    @Override
    public void stop() {
        super.stop();
        executorService.shutdown();
    }
}
