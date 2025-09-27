package com.langa.agent.core.buffers;

import com.langa.agent.core.services.SenderService;
import com.langa.agent.core.model.LogEntry;
import com.langa.agent.core.model.LogRequestDto;

import java.util.ArrayList;
import java.util.List;

public class LogBuffer extends AbstractBuffer<LogEntry> {

    public LogBuffer(SenderService senderService, String appKey, String accountKey,
                     int batchSize, int flushIntervalSeconds) {
        super(senderService, appKey, accountKey, batchSize, flushIntervalSeconds);
    }

    @Override
    protected synchronized void flush() {
        if (mainQueue.isEmpty()) {
            return;
        }

        List<LogEntry> logs = new ArrayList<>();
        mainQueue.drainTo(logs, batchSize);

        if (!logs.isEmpty()) {
            LogRequestDto dto = new LogRequestDto(appKey, accountKey, logs);
            boolean isSendSuccess = senderService.send(dto);
            if(!isSendSuccess) {
                logs.forEach(logEntry -> {
                    consecutiveSendingErrors++;
                    if (!retryQueue.offer(logEntry)) System.err.println("Entry lost " + logEntry);
                });
            } else {
                consecutiveSendingErrors = 0;
            }
        }
    }

    @Override
    protected void retryFlush() {
        if (retryQueue.isEmpty()) {
            return;
        }

        //retry logic here
        int retryDelay = (int) Math.pow(2, consecutiveSendingErrors);
        //scheduler.schedule(this::retryFlush, )

    }


}
