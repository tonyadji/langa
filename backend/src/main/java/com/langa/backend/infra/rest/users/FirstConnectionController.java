package com.langa.backend.infra.rest.users;

import com.langa.backend.domain.users.usecases.GetUserUseCase;
import com.langa.backend.infra.rest.users.dto.CompleteFirstConnectionRequestDto;
import com.langa.backend.infra.rest.users.dto.UserDto;
import com.langa.backend.infra.services.users.CompleteFirstConnectionService;
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
    private final CompleteFirstConnectionService completeFirstConnectionService;

    public FirstConnectionController(GetUserUseCase getUserUseCase,
                                     CompleteFirstConnectionService completeFirstConnectionService) {
        this.getUserUseCase = getUserUseCase;
        this.completeFirstConnectionService = completeFirstConnectionService;
    }


    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(@RequestParam String token) {
        return ResponseEntity.ok(UserDto.of(getUserUseCase.findByFirstConnectionToken(token)));
    }

    @PostMapping("/complete")
    public ResponseEntity<String> completeFirstConnectionProcess(@Valid @RequestBody CompleteFirstConnectionRequestDto requestDto) {
        completeFirstConnectionService.complete(requestDto.firstConnectionToken(), requestDto.toUpdatePassword());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Account setup completed");
    }
}
