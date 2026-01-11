package com.langa.backend.infra.rest.users;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.users.usecases.fetch.IGetUserUseCase;
import com.langa.backend.domain.users.usecases.logout.LogoutCommand;
import com.langa.backend.infra.rest.users.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final IGetUserUseCase getUserUseCase;
    private final CommandBusDispatcher commandBusDispatcher;

    public UsersController(IGetUserUseCase getUserUseCase, CommandBusDispatcher commandBusDispatcher) {
        this.getUserUseCase = getUserUseCase;
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @GetMapping("me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(UserDto.of(getUserUseCase.queryByUsername(userDetails.getUsername())));
    }

    @PostMapping("logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserDetails userDetails) {
        commandBusDispatcher.dispatch(new LogoutCommand(userDetails.getUsername()));
        return ResponseEntity.ok("User Logged out");
    }
}
