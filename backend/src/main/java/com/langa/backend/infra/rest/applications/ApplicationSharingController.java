package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.infra.rest.applications.dto.ApplicationDto;
import com.langa.backend.infra.rest.applications.dto.ShareAppRequestDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationSharingController {

    private final CommandBusDispatcher commandBusDispatcher;

    public ApplicationSharingController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @PostMapping("{appId}/share")
    public ResponseEntity<ApplicationDto> shareApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable String appId,
                                                      @RequestBody @Valid ShareAppRequestDto requestDto) {
        final ApplicationInfo applicationInfo = commandBusDispatcher.dispatch(requestDto.toShareCommand(appId, userDetails.getUsername()));
        final ApplicationDto applicationDto = ApplicationDto.of(applicationInfo);
        return ResponseEntity.ok(applicationDto);
    }

    @PostMapping("{appId}/revoke-sharing")
    public ResponseEntity<ApplicationDto> revokeApplicationSharing(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable String appId,
                                                      @RequestBody @Valid ShareAppRequestDto requestDto) {
        final ApplicationInfo applicationInfo = commandBusDispatcher.dispatch(requestDto.toRevokeCommand(appId, userDetails.getUsername()));
        final ApplicationDto applicationDto = ApplicationDto.of(applicationInfo);
        return ResponseEntity.ok(applicationDto);
    }
}
