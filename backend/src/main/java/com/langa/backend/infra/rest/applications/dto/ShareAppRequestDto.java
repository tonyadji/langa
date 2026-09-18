package com.langa.backend.infra.rest.applications.dto;

import com.langa.backend.domain.applications.usecases.sharing.revoke.RevokeSharingApplicationCommand;
import com.langa.backend.domain.applications.usecases.sharing.share.ShareApplicationCommand;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ShareAppRequestDto(
        @NotBlank String shareWith,
        @NotNull SharedWithProfile profile
) {

    public ShareApplicationCommand toShareCommand(String appId, String userName) {
        return new ShareApplicationCommand(
                appId,
                userName,
                shareWith,
                profile
        );
    }

    public RevokeSharingApplicationCommand toRevokeCommand(String appId, String username) {
        return new RevokeSharingApplicationCommand(
                appId,
                username,
                shareWith,
                profile
        );
    }
}
