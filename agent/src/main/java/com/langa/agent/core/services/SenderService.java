package com.langa.agent.core.services;

import com.langa.agent.core.model.SendableRequestDto;

public interface SenderService {
    boolean send(SendableRequestDto payload);
}
