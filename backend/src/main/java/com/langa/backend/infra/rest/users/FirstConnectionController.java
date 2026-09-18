package com.langa.backend.infra.rest.users;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.users.usecases.fetch.IGetUserUseCase;
import com.langa.backend.infra.rest.users.dto.CompleteFirstConnectionRequestDto;
import com.langa.backend.infra.rest.users.dto.UserDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/first-connection")
@Slf4j
public class FirstConnectionController {

    private final IGetUserUseCase getUserUseCase;
    private final CommandBusDispatcher commandBusDispatcher;

    public FirstConnectionController(IGetUserUseCase getUserUseCase,
                                     CommandBusDispatcher commandBusDispatcher) {
        this.getUserUseCase = getUserUseCase;
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(@RequestParam String token) {
        return ResponseEntity.ok(UserDto.of(getUserUseCase.queryByFirstConnectionToken(token)));
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeFirstConnectionProcess(@Valid @RequestBody CompleteFirstConnectionRequestDto requestDto) {
        commandBusDispatcher.dispatch(requestDto.toCommand());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Account setup completed");
    }
}
