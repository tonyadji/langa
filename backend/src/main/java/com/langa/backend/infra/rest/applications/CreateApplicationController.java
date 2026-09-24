package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.infra.rest.applications.dto.ApplicationDto;
import com.langa.backend.infra.rest.applications.dto.CreateApplicationRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/applications")
public class CreateApplicationController {

    private final CommandBusDispatcher commandBusDispatcher;

    public CreateApplicationController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApplicationDto> createApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                            @Valid @RequestBody CreateApplicationRequestDto applicationRequestDto) {

        return ResponseEntity.status(CREATED)
                .body(ApplicationDto.of(commandBusDispatcher.dispatch(applicationRequestDto.toCommand(userDetails.getUsername()))));
    }
}
