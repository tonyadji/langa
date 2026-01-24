package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.config.AgentConfig;
import com.capricedumardi.agent.core.config.ConfigLoader;
import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.config.jmx.AgentDynamicConfig;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.util.Timeout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

public class HttpSenderService implements SenderService {

    // Instance fields
    private final String url;
    private final CredentialsHelper credentialsHelper;
    private final CloseableHttpClient httpClient;
    private final Gson gson;
    private final CircuitBreaker circuitBreaker;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    // Statistics (for monitoring)
    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalCompressed = new AtomicLong(0);

    private final AgentConfig agentConfig;
    private final AgentDynamicConfig dynamicConfig; //TODO use it to take advantage on dynamic config

    public HttpSenderService(String url, CredentialsHelper credentialsHelper, AgentDynamicConfig dynamicConfig) {
        this.url = url;
        this.credentialsHelper = credentialsHelper;
        agentConfig = ConfigLoader.getConfigInstance();
        this.dynamicConfig = dynamicConfig;
        this.gson = new Gson();
        this.circuitBreaker = new CircuitBreaker("HTTP[" + url + "]",
                dynamicConfig.getCircuitBreakerFailureThreshold(),
                dynamicConfig.getCircuitBreakerOpenDurationMillis());

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(dynamicConfig.getHttpMaxConnectionsTotal());
        connectionManager.setDefaultMaxPerRoute(dynamicConfig.getHttpMaxConnectionsPerRoute());

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.of(dynamicConfig.getHttpConnectionRequestTimeoutMillis(), TimeUnit.MILLISECONDS))
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .build();

        LangaPrinter.printTrace("HttpSenderService initialized: " + url);
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        if (closed.get()) {
            return false;
        }

        if (!circuitBreaker.allowRequest()) {
            return false;
        }

        boolean success = sendWithRetry(payload);

        if (success) {
            circuitBreaker.recordSuccess();
            totalSent.incrementAndGet();
        } else {
            circuitBreaker.recordFailure();
            totalFailed.incrementAndGet();
        }

        return success;
    }

    private boolean sendWithRetry(SendableRequestDto payload) {
        int attempt = 0;
        Exception lastException = null;
        final int maxRetryAttempts = dynamicConfig.getHttpMaxRetryAttempts();
        while (attempt < maxRetryAttempts) {
            try {
                return sendOnce(payload);

            } catch (IOException e) {
                lastException = e;
                attempt++;

                if (attempt < maxRetryAttempts) {
                    int delay = calculateRetryDelay(attempt);
                    LangaPrinter.printError("HttpSenderService: Send failed (attempt " + attempt +
                            "/" + maxRetryAttempts + "), retrying in " + delay + "ms: " +
                            e.getMessage());

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }

            } catch (Exception e) {
                LangaPrinter.printError("HttpSenderService: Unexpected error sending payload: " +
                        e.getClass().getSimpleName() + ": " + e.getMessage());
                return false;
            }
        }

        LangaPrinter.printError("HttpSenderService: All " + maxRetryAttempts +
                " retry attempts failed. Last error: " +
                (lastException != null ? lastException.getMessage() : "unknown"));
        return false;
    }

    /**
     * Single send attempt without retry.
     *
     * @return true if send succeeded (2xx response), false otherwise
     * @throws IOException if network error occurs (caller should retry)
     */
    private boolean sendOnce(SendableRequestDto payload) throws IOException {
        String json = gson.toJson(payload);

        HttpPost httpPost = new HttpPost(url);

        addCredentialHeaders(httpPost);

        HttpEntity entity;
        if (dynamicConfig.isHttpCompressionEnabled() && json.length() > dynamicConfig.getHttpCompressionThresholdBytes()) {
            entity = createCompressedEntity(json);
            httpPost.addHeader("Content-Encoding", "gzip");
            totalCompressed.incrementAndGet();
        } else {
            entity = new ByteArrayEntity(
                    json.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    ContentType.APPLICATION_JSON
            );
        }

        httpPost.setEntity(entity);

        try (CloseableHttpResponse response = httpClient.execute(httpPost,
                HttpClientContext.create(),
                response1 ->
                    CloseableHttpResponse.create(response1, (closeable, closeMode) ->
                        closeable.close())
                )) {
            int statusCode = response.getCode();

            if (statusCode >= 200 && statusCode < 300) {
                return true;

            } else if (statusCode == 429) {
                LangaPrinter.printError("HttpSenderService: Rate limited (429), will retry");
                throw new IOException("Rate limited");

            } else if (statusCode >= 500) {
                LangaPrinter.printError("HttpSenderService: Server error (" + statusCode + "), will retry");
                throw new IOException("Server error: " + statusCode);

            } else if (statusCode >= 400) {
                LangaPrinter.printError("HttpSenderService: Client error (" + statusCode + "), not retrying");
                return false;

            } else {
                LangaPrinter.printError("HttpSenderService: Unexpected status code: " + statusCode);
                return false;
            }
        }
    }

    /**
     * Add authentication headers to request.
     */
    private void addCredentialHeaders(HttpPost httpPost) {
        try {
            List<? extends Header> headers = credentialsHelper.getCredentials(
                            CredentialsHelper.CredentialType.HTTP
                    )
                    .entrySet()
                    .stream()
                    .map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                    .toList();

            headers.forEach(httpPost::addHeader);

        } catch (Exception e) {
            LangaPrinter.printError("HttpSenderService: Error adding credential headers: " + e.getMessage());
        }
    }

    /**
     * Create GZIP-compressed entity from JSON string.
     */
    private HttpEntity createCompressedEntity(String json) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipStream = new GZIPOutputStream(byteStream)) {
            gzipStream.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        byte[] compressedData = byteStream.toByteArray();

        return new ByteArrayEntity(compressedData, ContentType.APPLICATION_JSON);
    }

    /**
     * Calculate retry delay with exponential backoff and jitter.
     */
    private int calculateRetryDelay(int attempt) {
        int delay = dynamicConfig.getHttpBaseRetryDelayMillis() * (int) Math.pow(2, attempt);

        int jitter = (int) (delay * 0.5 * new Random().nextInt());

        return Math.min(delay + jitter, dynamicConfig.getHttpMaxRetryDelayMillis());
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            LangaPrinter.printTrace("Closing HttpSenderService: " + url);

            try {
                httpClient.close();
                LangaPrinter.printTrace("HttpSenderService closed successfully");

            } catch (IOException e) {
                LangaPrinter.printError("Error closing HttpSenderService: " + e.getMessage());
            }
        }
    }

    @Override
    public String getDescription() {
        return "HTTP[" + url + ", circuit=" + circuitBreaker.getState() + "]";
    }

    /**
     * Get total number of successful sends.
     * Useful for monitoring.
     */
    public long getTotalSent() {
        return totalSent.get();
    }

    /**
     * Get total number of failed sends.
     * Useful for monitoring.
     */
    public long getTotalFailed() {
        return totalFailed.get();
    }

    /**
     * Get total number of compressed payloads.
     * Useful for monitoring compression effectiveness.
     */
    public long getTotalCompressed() {
        return totalCompressed.get();
    }

    /**
     * Get circuit breaker state.
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }
}
