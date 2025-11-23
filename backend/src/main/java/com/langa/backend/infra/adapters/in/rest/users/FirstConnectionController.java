package com.langa.backend.infra.adapters.in.rest.users;

import com.langa.backend.domain.users.usecases.getinfo.GetUserUseCase;
import com.langa.backend.infra.adapters.in.rest.users.dto.CompleteFirstConnectionRequestDto;
import com.langa.backend.infra.adapters.in.rest.users.dto.UserDto;
import com.langa.backend.application.services.users.CompleteRegistrationProcessService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/first-connection")
@Slf4j
public class FirstConnectionController {

    private final GetUserUseCase getUserUseCase;
    private final CompleteRegistrationProcessService completeRegistrationProcessService;

    public FirstConnectionController(GetUserUseCase getUserUseCase,
                                     CompleteRegistrationProcessService completeRegistrationProcessService) {
        this.getUserUseCase = getUserUseCase;
        this.completeRegistrationProcessService = completeRegistrationProcessService;
    }


    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(@RequestParam String token) {
        return ResponseEntity.ok(UserDto.of(getUserUseCase.findByFirstConnectionToken(token)));
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeRegistrationProcess(@Valid @RequestBody CompleteFirstConnectionRequestDto requestDto) {
        completeRegistrationProcessService.complete(requestDto.toCommand());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Account setup completed");
    }
}
