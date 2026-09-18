package com.capricedumardi.agent.testsupport;

import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.services.SenderService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test double for SenderService: records every payload handed to it instead of sending it
 * anywhere, and lets a test control whether the next send() succeeds or fails.
 */
public class RecordingSenderService implements SenderService {

    private final List<SendableRequestDto> sentPayloads = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger sendCallCount = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private volatile boolean nextResultSuccess = true;

    public void succeedNext() {
        this.nextResultSuccess = true;
    }

    public void failNext() {
        this.nextResultSuccess = false;
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        sendCallCount.incrementAndGet();
        if (nextResultSuccess) {
            sentPayloads.add(payload);
        }
        return nextResultSuccess;
    }

    @Override
    public void close() {
        closed.set(true);
    }

    @Override
    public String getDescription() {
        return "RecordingSenderService";
    }

    public List<SendableRequestDto> sentPayloads() {
        return new ArrayList<>(sentPayloads);
    }

    public int sendCallCount() {
        return sendCallCount.get();
    }

    public boolean isClosed() {
        return closed.get();
    }

    public void reset() {
        sentPayloads.clear();
        sendCallCount.set(0);
        nextResultSuccess = true;
    }
}
