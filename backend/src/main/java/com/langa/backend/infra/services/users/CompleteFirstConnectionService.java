package com.langa.backend.infra.services.users;

import com.langa.backend.domain.users.usecases.CompleteFirstConnectionUseCase;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompleteFirstConnectionService {

    private final CompleteFirstConnectionUseCase completeFirstConnectionUseCase;

    public CompleteFirstConnectionService(CompleteFirstConnectionUseCase completeFirstConnectionUseCase) {
        this.completeFirstConnectionUseCase = completeFirstConnectionUseCase;
    }

    @Transactional
    public void complete(String firstConnectionToken, UpdatePassword updatePassword) {
        completeFirstConnectionUseCase.complete(firstConnectionToken, updatePassword);
    }
}
