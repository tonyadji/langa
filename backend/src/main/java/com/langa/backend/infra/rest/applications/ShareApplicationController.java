package com.langa.backend.infra.rest.applications;

import com.langa.backend.domain.applications.valueobjects.ShareWith;
import com.langa.backend.infra.rest.applications.dto.ShareAppRequestDto;
import com.langa.backend.infra.services.applications.ShareApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ShareApplicationController {

    private final ShareApplicationService shareApplicationService;

    public ShareApplicationController(ShareApplicationService shareApplicationService) {
        this.shareApplicationService = shareApplicationService;
    }

    @PostMapping("{appId}/share")
    public ResponseEntity<ShareWith> shareApplication(@AuthenticationPrincipal UserDetails userDetails,
                                                      @PathVariable String appId,
                                                      @RequestBody @Valid ShareAppRequestDto request) {
        return ResponseEntity.ok(shareApplicationService.shareApplication(appId, userDetails.getUsername(), request.sharedWith(), request.profile()));
    }
}
