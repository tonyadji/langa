package com.langa.backend.infra.rest.users;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.infra.rest.users.dto.LoginRequestDto;
import com.langa.backend.infra.rest.users.dto.LoginResponseDto;
import com.langa.backend.infra.rest.users.dto.RefreshRequestDto;
import com.langa.backend.infra.rest.users.dto.RegisterRequestDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final CommandBusDispatcher commandBusDispatcher;

    public AuthController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDto registerRequestDto) {
        log.info("Register request: {}", registerRequestDto.username());
        commandBusDispatcher.dispatch(registerRequestDto.toCommand());
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(LoginResponseDto.of(commandBusDispatcher.dispatch(loginRequestDto.toCommand())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshRequestDto body) {
        var tokens = commandBusDispatcher.dispatch(body.toCommand());
        return ResponseEntity.ok(LoginResponseDto.of(tokens));
    }
}
