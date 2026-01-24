package com.langa.backend.common.commands;

public interface CommandBusDispatcher {

    <C extends Command<R>, R> R dispatch(C command);
}
