package com.langa.agent.core.buffers;

import com.langa.agent.core.model.MetricEntry;
import com.langa.agent.core.model.MetricRequestDto;
import com.langa.agent.core.services.SenderService;

import java.util.ArrayList;
import java.util.List;

public class MetricBuffer extends AbstractBuffer<MetricEntry> {

    public MetricBuffer(SenderService senderService, String appKey, String accountKey,
                        int batchSize, int flushIntervalSeconds) {
        super(senderService, appKey, accountKey, batchSize, flushIntervalSeconds);
    }

    @Override
    protected synchronized void flush() {
        if (mainQueue.isEmpty()) {
            return;
        }

        List<MetricEntry> metricEntries = new ArrayList<>();
        mainQueue.drainTo(metricEntries, batchSize);

        if (!metricEntries.isEmpty()) {
            MetricRequestDto dto = new MetricRequestDto(appKey, accountKey, metricEntries);
            senderService.send(dto);
        }
    }

    @Override
    protected void retryFlush() {
        if (retryQueue.isEmpty()) {
            return;
        }

        //retry logic here
    }
}
