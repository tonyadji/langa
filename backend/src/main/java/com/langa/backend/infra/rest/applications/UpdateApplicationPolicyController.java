package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.infra.config.LangaApplicationProperties;
import com.langa.backend.infra.rest.applications.dto.SecuredApplicationDto;
import com.langa.backend.infra.rest.applications.dto.UpdateApplicationRetentionPolicyDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/applications")
public class UpdateApplicationPolicyController {

    private final LangaApplicationProperties applicationProperties;
    private final CommandBusDispatcher commandBusDispatcher;

    public UpdateApplicationPolicyController(LangaApplicationProperties applicationProperties, CommandBusDispatcher commandBusDispatcher) {
        this.applicationProperties = applicationProperties;
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @PutMapping("{appId}/update-retention-policy")
    public ResponseEntity<SecuredApplicationDto> createApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @PathVariable String appId,
                                                                   @Valid @RequestBody UpdateApplicationRetentionPolicyDto updateDto) {

        return ResponseEntity.status(OK)
                .body(SecuredApplicationDto.of(commandBusDispatcher.dispatch(updateDto.toCommand(appId, userDetails.getUsername())),
                        applicationProperties.getHttpPrefix(),
                        applicationProperties.getKafkaPrefix()));
    }
}
