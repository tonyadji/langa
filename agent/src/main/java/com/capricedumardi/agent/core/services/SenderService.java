package com.capricedumardi.agent.core.services;

import com.capricedumardi.agent.core.model.SendableRequestDto;

public interface SenderService {
    boolean send(SendableRequestDto payload);
}
