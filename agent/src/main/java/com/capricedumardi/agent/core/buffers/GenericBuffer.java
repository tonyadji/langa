package com.capricedumardi.agent.core.buffers;

import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.model.SendableRequestDto;
import com.capricedumardi.agent.core.services.SenderService;

import java.util.List;
import java.util.function.Function;

public class GenericBuffer<T, U extends SendableRequestDto> extends AbstractBuffer<T> {
    private final Function<List<T>, U> mapSendableRequestDto;

    public GenericBuffer(Function<List<T>, U> mapSendableRequestDto,
                         SenderService senderService, String appKey, String accountKey,
                     int batchSize, int flushIntervalSeconds, String bufferName) {
        super(senderService, appKey, accountKey, batchSize, flushIntervalSeconds, bufferName);
        this.mapSendableRequestDto = mapSendableRequestDto;
    }

    @Override
    protected SendableRequestDto mapToSendableRequest(List<T> entries) {
        return mapSendableRequestDto.apply(entries);
    }

    public void printStats() {
        BufferStats stats = getStats();
        LangaPrinter.printTrace("\n" + stats.toString());
    }

}
