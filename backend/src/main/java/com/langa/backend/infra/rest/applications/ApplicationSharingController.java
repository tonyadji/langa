package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.usecases.RevokeSharingUseCase;
import com.langa.backend.domain.applications.valueobjects.ShareWith;
import com.langa.backend.infra.rest.applications.dto.ShareAppRequestDto;
import com.langa.backend.infra.adapters.services.applications.ShareApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationSharingController {

    private final ShareApplicationService shareApplicationService;
    private final RevokeSharingUseCase revokeSharingUseCase;

    public ApplicationSharingController(ShareApplicationService shareApplicationService, RevokeSharingUseCase revokeSharingUseCase) {
        this.shareApplicationService = shareApplicationService;
        this.revokeSharingUseCase = revokeSharingUseCase;
    }

    @PostMapping("{appId}/share")
    public ResponseEntity<ShareWith> shareApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable String appId,
                                                      @RequestBody @Valid ShareAppRequestDto request) {
        return ResponseEntity.ok(shareApplicationService.shareApplication(appId, userDetails.getUsername(), request.sharedWith(), request.profile()));
    }

    @PostMapping("{appId}/revoke")
    public ResponseEntity<ShareWith> revokeApplicationSharing(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable String appId,
                                                      @RequestBody @Valid ShareAppRequestDto request) {
        revokeSharingUseCase.revokeSharing(appId, userDetails.getUsername(), request.sharedWith(), request.profile());
        return ResponseEntity.noContent().build();
    }
}
