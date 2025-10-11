package com.langa.backend.infra.services.applications;

import com.langa.backend.domain.applications.usecases.ShareApplicationUseCase;
import com.langa.backend.domain.applications.valueobjects.ShareWith;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShareApplicationService {
    private final ShareApplicationUseCase shareApplicationUseCase;

    public ShareApplicationService(ShareApplicationUseCase shareApplicationUseCase) {
        this.shareApplicationUseCase = shareApplicationUseCase;
    }

    @Transactional
    public ShareWith shareApplication(String appId, String owner, String sharedWith, SharedWithProfile  profile) {
        return shareApplicationUseCase.shareWith(appId, owner, sharedWith, profile);
    }
}
