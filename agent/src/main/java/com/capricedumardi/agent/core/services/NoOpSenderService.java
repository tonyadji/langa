package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.model.SendableRequestDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class NoOpSenderService implements  SenderService {
    private static final Logger log = LogManager.getLogger(NoOpSenderService.class);
    private final String reason;
    public NoOpSenderService(String reason) {
        this.reason = reason;
        log.error("ATTENTION: SenderService is in No-Op mode. Logs/metrics WILL NOT be sent. Reason: {}", reason);
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        log.error("Unable to send because : {}", reason);
        return false;
    }
}
