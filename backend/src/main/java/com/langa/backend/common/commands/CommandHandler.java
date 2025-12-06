package com.langa.backend.common.commands;

public interface CommandHandler<C extends Command<R>, R> {

    <R> R handle(C command) ;
}
