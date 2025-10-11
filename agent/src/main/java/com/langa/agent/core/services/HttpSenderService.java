package com.langa.agent.core.services;

import com.google.gson.Gson;
import com.langa.agent.core.model.SendableRequestDto;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpSenderService implements SenderService {
    private static final Logger log = LogManager.getLogger(HttpSenderService.class);
    private static final CloseableHttpClient httpClient = HttpClients.createDefault();
    private static final Gson gson = new Gson();
    private final String url;

    public HttpSenderService(String url) {
        this.url = url;
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        try {
            String json = gson.toJson(payload);
            HttpPost httpPost = new HttpPost(url);
            httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() >= 400) {
                    log.error("HttpSenderService - Failed with status {}",response.getStatusLine().getStatusCode());
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
            log.error("HttpSenderService - Error sending payload: {}",e.getMessage());
        }
        return false;
    }
}
