package com.capricedumardi.agent.core.services;

import com.google.gson.Gson;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpSenderService implements SenderService {
    private static final Logger log = LogManager.getLogger(HttpSenderService.class);
    private static final int MAXIMUM_CONNECTIONS = 100;
    private static final int MAXIMUM_CONNECTIONS_PER_ROUTE = 20;
    private static final int REQUEST_TIMEOUT_MILLIS = 5000;
    private static final int SOCKET_TIMEOUT_MILLIS = 10000;
    private static final int POOL_CONNECTION_TIMEOUT_MILLIS = 30000;

    private static final CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionTimeToLive(1, TimeUnit.MINUTES)
            .setMaxConnTotal(MAXIMUM_CONNECTIONS)
            .setMaxConnPerRoute(MAXIMUM_CONNECTIONS_PER_ROUTE)
            .setRetryHandler(new DefaultHttpRequestRetryHandler(3, true))
            .setDefaultRequestConfig(RequestConfig.custom()
                    .setConnectTimeout(REQUEST_TIMEOUT_MILLIS)
                    .setSocketTimeout(SOCKET_TIMEOUT_MILLIS)
                    .setConnectionRequestTimeout(POOL_CONNECTION_TIMEOUT_MILLIS)
                    .build())
            .build();
    private static final Gson gson = new Gson();
    private final String url;
    private final CredentialsHelper credentialsHelper;

    public HttpSenderService(String url, CredentialsHelper credentialshelper) {
        this.url = url;
        this.credentialsHelper = credentialshelper;
    }

    private List<? extends Header> getCredentials() {
        return this.credentialsHelper.getCredentials(CredentialsHelper.CredentialType.HTTP)
                .entrySet()
                .stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        try {
            String json = gson.toJson(payload);
            HttpPost httpPost = new HttpPost(url);

            getCredentials().forEach(httpPost::addHeader);

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
