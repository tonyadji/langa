package com.langa.backend.application.services;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.commands.Command;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ApplicationCommandBus implements CommandBusDispatcher {

    private final Map<Class<? extends Command>, CommandHandler> registry;

    public ApplicationCommandBus (List<CommandHandler> handlers) {
        registry = new HashMap<>();
        for (CommandHandler commandHandler : handlers) {
            registerHandler(commandHandler);
        }
    }

    @Transactional
    @Override
    public <C extends Command<R>, R> R dispatch(C command) {
        final CommandHandler<C, R> handler = resolveHandler(command);
        log.debug(String.format("Dispatching command %s to handler %s", command, handler.getClass()));
        return handler.handle(command);
    }

    private CommandHandler resolveHandler(Command command) {
        return registry.get(command.getClass());
    }

    private void registerHandler(CommandHandler handler) {
        Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(handler.getClass(), CommandHandler.class);

        if (generics == null) {
            throw new IllegalStateException("Unable to determine command type for handler : " + handler.getClass().getName());
        }

        Class<? extends Command> commandType = (Class<? extends Command>) generics[0];

        if (registry.containsKey(commandType)) {
            throw new IllegalStateException(String.format(
                    "FATAL ERROR : Handler duplication ! The '%s' command is already handled by '%s'. Can't assign it to '%s'.",
                    commandType.getSimpleName(),
                    registry.get(commandType).getClass().getSimpleName(),
                    handler.getClass().getSimpleName()
            ));
        }

        registry.put(commandType, handler);
        log.debug("Handler registered : {} -> {}", commandType.getSimpleName(), handler.getClass().getSimpleName());
    }
}
