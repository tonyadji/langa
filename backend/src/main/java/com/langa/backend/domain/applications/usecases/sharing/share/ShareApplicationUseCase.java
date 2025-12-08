package com.langa.backend.domain.applications.usecases.sharing.share;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.eda.services.OutboxEventService;
import com.langa.backend.common.model.ShareWithInfo;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.events.ApplicationSharedEvent;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import com.langa.backend.domainexchange.teams.TeamService;
import com.langa.backend.domainexchange.user.UserAccountService;

import java.util.Objects;

@UseCase
public class ShareApplicationUseCase implements IShareApplicationUseCase, CommandHandler<ShareApplicationCommand, ApplicationInfo> {

    private final ApplicationRepository applicationRepository;
    private final TeamService teamService;
    private final UserAccountService userAccountService;
    private final OutboxEventService outboxEventService;

    public ShareApplicationUseCase(ApplicationRepository applicationRepository,
                                   TeamService teamService,
                                   UserAccountService userAccountService,
                                   OutboxEventService outboxEventService) {
        this.applicationRepository = applicationRepository;
        this.teamService = teamService;
        this.userAccountService = userAccountService;
        this.outboxEventService = outboxEventService;
    }

    @Override
    public ApplicationInfo execute(ShareApplicationCommand command) {
        final Application app =  applicationRepository.findById(command.appId())
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));
        app.checkOwnership(command.owner());

        final ShareWithInfo accountOrTeamInfo = getShareWithInfo(command.shareWith(), command.profile());

        if (Objects.equals(accountOrTeamInfo.email(), command.owner()) && SharedWithProfile.USER == command.profile()) {
            throw new ApplicationException("Auto share not authorized ", null, Errors.APPLICATION_AUTO_SHARE_FORBIDDEN);
        }

        if (app.alreadySharedWith(accountOrTeamInfo.key())) {
            throw new ApplicationException("Application already shared with " + accountOrTeamInfo.email(), null, Errors.APPLICATION_ALREADY_SHARED);
        }

        app.shareWith(accountOrTeamInfo.key(), command.profile());

        applicationRepository.save(app);

        outboxEventService.storeOutboxEvent(new ApplicationSharedEvent(app.getId(), command.owner(), accountOrTeamInfo.email(), app.getName()));

        return ApplicationInfo.of(app);
    }

    private ShareWithInfo getShareWithInfo(String shareWith, SharedWithProfile profile) {
        return SharedWithProfile.USER == profile ? userAccountService.getShareWithInfo(shareWith) : teamService.getShareWithInfo(shareWith);
    }

    @Override
    public ApplicationInfo handle(ShareApplicationCommand command) {
        return execute(command);
    }
}
