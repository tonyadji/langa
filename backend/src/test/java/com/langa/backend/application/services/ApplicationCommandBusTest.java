package com.langa.backend.application.services;

import com.langa.backend.common.commands.Command;
import com.langa.backend.common.commands.CommandHandler;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationCommandBusTest {

    record CommandA(String value) implements Command<String> {}
    record CommandB(String value) implements Command<String> {}

    static class HandlerA implements CommandHandler<CommandA, String> {
        @Override
        public String handle(CommandA command) {
            return "A:" + command.value();
        }
    }

    static class HandlerB implements CommandHandler<CommandB, String> {
        @Override
        public String handle(CommandB command) {
            return "B:" + command.value();
        }
    }

    static class DuplicateHandlerA implements CommandHandler<CommandA, String> {
        @Override
        public String handle(CommandA command) {
            return "duplicate";
        }
    }

    static class RawHandler implements CommandHandler {
        @Override
        public Object handle(Command command) {
            return "raw";
        }
    }

    @Test
    void dispatch_shouldRouteToCorrectHandler() {
        ApplicationCommandBus bus = new ApplicationCommandBus(List.of(new HandlerA(), new HandlerB()));

        assertEquals("A:hello", bus.dispatch(new CommandA("hello")));
        assertEquals("B:world", bus.dispatch(new CommandB("world")));
    }

    @Test
    void constructor_shouldThrow_whenDuplicateHandlerForSameCommand() {
        assertThrows(IllegalStateException.class,
                () -> new ApplicationCommandBus(List.of(new HandlerA(), new DuplicateHandlerA())));
    }

    @Test
    void constructor_shouldThrow_whenHandlerHasNoResolvableGenericType() {
        assertThrows(IllegalStateException.class, () -> new ApplicationCommandBus(List.of(new RawHandler())));
    }
}
