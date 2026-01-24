package com.langa.backend.domain.users.usecases.completefirstconnection;

import com.langa.backend.common.commands.Command;
import com.langa.backend.domain.users.valueobjects.UpdatePassword;

public record CompleteFirstConnectionCommand(
        String firstConnectionToken, UpdatePassword updatePassword
) implements Command<String> {
}
