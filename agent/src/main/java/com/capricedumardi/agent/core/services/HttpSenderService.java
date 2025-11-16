package com.capricedumardi.agent.core.services;

import com.google.gson.Gson;
import com.capricedumardi.agent.core.helpers.CredentialsHelper;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

public class HttpSenderService implements SenderService {
    // Configuration constants
    private static final int MAX_CONNECTIONS_TOTAL = 100;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    private static final int CONNECT_TIMEOUT_MILLIS = 5000;      // 5 seconds
    private static final int SOCKET_TIMEOUT_MILLIS = 10000;      // 10 seconds
    private static final int CONNECTION_REQUEST_TIMEOUT = 30000; // 30 seconds
    private static final int COMPRESSION_THRESHOLD_BYTES = 1024; // 1 KB

    // Retry configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int BASE_RETRY_DELAY_MS = 100;
    private static final int MAX_RETRY_DELAY_MS = 5000;

    // Circuit breaker configuration
    private static final int CIRCUIT_BREAKER_THRESHOLD = 5;
    private static final long CIRCUIT_BREAKER_TIMEOUT_MS = 30000; // 30 seconds

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


    public HttpSenderService(String url, CredentialsHelper credentialsHelper) {
        this.url = url;
        this.credentialsHelper = credentialsHelper;
        this.gson = new Gson();
        this.circuitBreaker = new CircuitBreaker("HTTP[" + url + "]",
                CIRCUIT_BREAKER_THRESHOLD,
                CIRCUIT_BREAKER_TIMEOUT_MS);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_CONNECTIONS_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT_MILLIS)
                .setSocketTimeout(SOCKET_TIMEOUT_MILLIS)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .build();

        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionTimeToLive(1, TimeUnit.MINUTES)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .build();

        System.out.println("HttpSenderService initialized: " + url);
    }

    private List<? extends Header> getCredentials() {
        return this.credentialsHelper.getCredentials(CredentialsHelper.CredentialType.HTTP)
                .entrySet()
                .stream().map(entry -> new BasicHeader(entry.getKey(), entry.getValue()))
                .toList();
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

        while (attempt < MAX_RETRY_ATTEMPTS) {
            try {
                return sendOnce(payload);

            } catch (IOException e) {
                lastException = e;
                attempt++;

                if (attempt < MAX_RETRY_ATTEMPTS) {
                    int delay = calculateRetryDelay(attempt);
                    System.err.println("HttpSenderService: Send failed (attempt " + attempt +
                            "/" + MAX_RETRY_ATTEMPTS + "), retrying in " + delay + "ms: " +
                            e.getMessage());

                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }

            } catch (Exception e) {
                System.err.println("HttpSenderService: Unexpected error sending payload: " +
                        e.getClass().getSimpleName() + ": " + e.getMessage());
                return false;
            }
        }

        System.err.println("HttpSenderService: All " + MAX_RETRY_ATTEMPTS +
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
        if (json.length() > COMPRESSION_THRESHOLD_BYTES) {
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

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return true;

            } else if (statusCode == 429) {
                System.err.println("HttpSenderService: Rate limited (429), will retry");
                throw new IOException("Rate limited");

            } else if (statusCode >= 500) {
                System.err.println("HttpSenderService: Server error (" + statusCode + "), will retry");
                throw new IOException("Server error: " + statusCode);

            } else if (statusCode >= 400) {
                System.err.println("HttpSenderService: Client error (" + statusCode + "), not retrying");
                return false;

            } else {
                System.err.println("HttpSenderService: Unexpected status code: " + statusCode);
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
            System.err.println("HttpSenderService: Error adding credential headers: " + e.getMessage());
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
        int delay = BASE_RETRY_DELAY_MS * (int) Math.pow(2, attempt - 1);

        int jitter = (int) (delay * 0.25 * Math.random());

        return Math.min(delay + jitter, MAX_RETRY_DELAY_MS);
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            System.out.println("Closing HttpSenderService: " + url);

            try {
                httpClient.close();
                System.out.println("HttpSenderService closed successfully");

            } catch (IOException e) {
                System.err.println("Error closing HttpSenderService: " + e.getMessage());
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
