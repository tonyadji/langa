package com.langa.backend.infra.rest.ingest.limits;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.infra.rest.advice.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Protects the public HTTP ingestion endpoint before the payload is parsed:
 * rate limit per application key (429) and maximum body size (413).
 * Registered on the ingestion path only, see {@link IngestionLimitsConfiguration}.
 */
@Slf4j
public class IngestionLimitsFilter extends OncePerRequestFilter {

    static final String APP_KEY_HEADER = "X-APP-KEY";

    private final IngestionLimitsProperties properties;
    private final IngestionRateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IngestionLimitsFilter(IngestionLimitsProperties properties, IngestionRateLimiter rateLimiter) {
        this.properties = properties;
        this.rateLimiter = rateLimiter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        final String appKey = request.getHeader(APP_KEY_HEADER);
        if (!rateLimiter.tryAcquire(appKey == null ? "" : appKey)) {
            log.warn("Ingestion rate limit exceeded for appKey {}", appKey);
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimiter.retryAfterSeconds()));
            reject(response, Errors.INGESTION_RATE_LIMITED, null);
            return;
        }

        final long maxPayloadBytes = properties.getMaxPayloadBytes();
        if (request.getContentLengthLong() > maxPayloadBytes) {
            log.warn("Ingestion payload too large for appKey {}: {} bytes", appKey, request.getContentLengthLong());
            reject(response, Errors.INGESTION_PAYLOAD_TOO_LARGE, "Maximum size: " + maxPayloadBytes + " bytes");
            return;
        }
        // Bodies without Content-Length (chunked) are limited while being read
        chain.doFilter(new LimitedBodyRequest(request, maxPayloadBytes), response);
    }

    private void reject(HttpServletResponse response, Errors error, String details) throws IOException {
        response.setStatus(error.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), ApiError.of(error.getMessage(), error.getCode(), details));
    }

    private static final class LimitedBodyRequest extends HttpServletRequestWrapper {
        private final long maxBytes;

        LimitedBodyRequest(HttpServletRequest request, long maxBytes) {
            super(request);
            this.maxBytes = maxBytes;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new LimitedInputStream(super.getInputStream(), maxBytes);
        }
    }

    private static final class LimitedInputStream extends ServletInputStream {
        private final ServletInputStream delegate;
        private final long maxBytes;
        private long read;

        LimitedInputStream(ServletInputStream delegate, long maxBytes) {
            this.delegate = delegate;
            this.maxBytes = maxBytes;
        }

        @Override
        public int read() throws IOException {
            final int value = delegate.read();
            if (value != -1) {
                count(1);
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            final int count = delegate.read(buffer, offset, length);
            if (count > 0) {
                count(count);
            }
            return count;
        }

        private void count(int bytes) {
            read += bytes;
            if (read > maxBytes) {
                throw new IngestionPayloadTooLargeException(maxBytes);
            }
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }
    }
}
