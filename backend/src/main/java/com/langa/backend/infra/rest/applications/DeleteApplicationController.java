package com.langa.backend.infra.rest.applications;

import com.langa.backend.common.commands.CommandBusDispatcher;
import com.langa.backend.domain.applications.usecases.delete.DeleteApplicationCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/applications")
public class DeleteApplicationController {

    private final CommandBusDispatcher commandBusDispatcher;

    public DeleteApplicationController(CommandBusDispatcher commandBusDispatcher) {
        this.commandBusDispatcher = commandBusDispatcher;
    }

    @DeleteMapping("{appId}")
    public ResponseEntity<String> createApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                                   @PathVariable String appId) {

        return ResponseEntity.status(OK)
                .body(commandBusDispatcher.dispatch(new DeleteApplicationCommand(appId, userDetails.getUsername())));
    }
}
