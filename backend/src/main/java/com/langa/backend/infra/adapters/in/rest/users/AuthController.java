package com.langa.backend.infra.adapters.in.rest.users;

import com.langa.backend.domain.users.usecases.login.LoginUseCase;
import com.langa.backend.domain.users.usecases.refreshaccesstoken.RefreshAccessTokenUseCase;
import com.langa.backend.domain.users.usecases.register.RegisterUseCase;
import com.langa.backend.infra.adapters.in.rest.users.dto.LoginRequestDto;
import com.langa.backend.infra.adapters.in.rest.users.dto.LoginResponseDto;
import com.langa.backend.infra.adapters.in.rest.users.dto.RefreshRequestDto;
import com.langa.backend.infra.adapters.in.rest.users.dto.RegisterRequestDto;
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
        registerUseCase.execute(registerRequestDto.toCommand());
        return ResponseEntity.ok("User registered");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {

        return ResponseEntity.ok(LoginResponseDto.of(loginUseCase.execute(loginRequestDto.toCommand())));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshRequestDto body) {
        var tokens = refreshAccessTokenUseCase.execute(body.toCommand());
        return ResponseEntity.ok(LoginResponseDto.of(tokens));
    }
}
