package com.capricedumardi.agent.core.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NoOpSenderServiceTest {

    @Test
    void sendAlwaysReturnsFalse() {
        NoOpSenderService service = new NoOpSenderService("no ingestion url configured");

        assertFalse(service.send(new LogRequestDtoStub()));
    }

    @Test
    void closeIsIdempotent() {
        NoOpSenderService service = new NoOpSenderService("test reason");

        service.close();
        service.close();
    }

    @Test
    void descriptionIsPresent() {
        NoOpSenderService service = new NoOpSenderService("test reason");

        assertNotNull(service.getDescription());
    }

    private static class LogRequestDtoStub implements com.capricedumardi.agent.core.model.SendableRequestDto {
    }
}
