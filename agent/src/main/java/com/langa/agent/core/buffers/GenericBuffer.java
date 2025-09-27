package com.langa.agent.core.buffers;

import com.langa.agent.core.model.SendableRequestDto;
import com.langa.agent.core.services.SenderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GenericBuffer<T, U extends SendableRequestDto> extends AbstractBuffer<T> {
    private static final Logger log = LogManager.getLogger(GenericBuffer.class);
    private final Function<List<T>, U> mapSendableRequestDto;

    public GenericBuffer(Function<List<T>, U> mapSendableRequestDto,
                         SenderService senderService, String appKey, String accountKey,
                     int batchSize, int flushIntervalSeconds) {
        super(senderService, appKey, accountKey, batchSize, flushIntervalSeconds);
        this.mapSendableRequestDto = mapSendableRequestDto;
    }

    @Override
    protected void flush() {
        flushAndCheck(mainQueue);
    }

    @Override
    protected void retryFlush() {
        flushAndCheck(retryQueue);
    }

    private void flushAndCheck(BlockingQueue<T> processingQueue) {
        if (processingQueue.isEmpty()) {
            return;
        }

        List<T> entries = new ArrayList<>();
        processingQueue.drainTo(entries, batchSize);

        if (!entries.isEmpty()) {
            U dto = mapSendableRequestDto.apply(entries);
            boolean isSendSuccess = senderService.send(dto);
            if(!isSendSuccess) {
                consecutiveSendingErrors++;
                entries.forEach(entry -> {
                    if (!retryQueue.offer(entry)) log.error("Entry lost {}", List.of(entry).toArray());
                });
                scheduleRetryFlush();
            } else {
                consecutiveSendingErrors = 0;
            }
        }
    }

    private void scheduleRetryFlush() {
        int retryDelay = (int) Math.pow(2, consecutiveSendingErrors);
        scheduler.schedule(this::retryFlush, retryDelay, TimeUnit.SECONDS);
    }
}
