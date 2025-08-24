package com.langa.backend.infra.rest.users;

import com.langa.backend.domain.users.usecases.LoginUseCase;
import com.langa.backend.domain.users.usecases.RefreshAccessTokenUseCase;
import com.langa.backend.domain.users.usecases.RegisterUseCase;
import com.langa.backend.infra.rest.users.dto.LoginRequestDto;
import com.langa.backend.infra.rest.users.dto.LoginResponseDto;
import com.langa.backend.infra.rest.users.dto.RefreshRequestDto;
import com.langa.backend.infra.rest.users.dto.RegisterRequestDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RegisterUseCase registerUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    public AuthController(LoginUseCase loginUseCase, RegisterUseCase registerUseCase, RefreshAccessTokenUseCase refreshAccessTokenUseCase) {
        this.loginUseCase = loginUseCase;
        this.registerUseCase = registerUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterRequestDto registerRequestDto) {
        log.info("Register request: {}", registerRequestDto.username());
        registerUseCase.register(registerRequestDto.username(), registerRequestDto.password(), registerRequestDto.confirmationPassword());
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {

        return ResponseEntity.ok(LoginResponseDto.of(loginUseCase.login(loginRequestDto.toAuthRequest())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshRequestDto body) {
        var tokens = refreshAccessTokenUseCase.refresh(body.refreshToken());
        return ResponseEntity.ok(LoginResponseDto.of(tokens));
    }
}
