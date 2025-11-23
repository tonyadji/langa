package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.config.LangaPrinter;
import com.capricedumardi.agent.core.model.SendableRequestDto;


public class NoOpSenderService implements  SenderService {
    private final String reason;
    private volatile boolean closed = false;

    public NoOpSenderService(String reason) {
        this.reason = reason;
        LangaPrinter.printWarning("SenderService is in No-Op mode. Logs/metrics WILL NOT be sent. Reason: "+reason);
    }

    @Override
    public boolean send(SendableRequestDto payload) {
        LangaPrinter.printConditionalError("Unable to send because : "+ reason);
        return false;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            LangaPrinter.printTrace("NoOpSenderService closed (reason: " + reason + ")");
        }
    }

    @Override
    public String getDescription() {
        return "No-OP Sender Service";
    }
}
