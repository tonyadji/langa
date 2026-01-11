package com.langa.backend.domain.applications.usecases.sharing.revoke;

import com.langa.backend.common.annotations.UseCase;
import com.langa.backend.common.commands.CommandHandler;
import com.langa.backend.common.model.ShareWithInfo;
import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.exceptions.ApplicationException;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.valueobjects.ApplicationInfo;
import com.langa.backend.domain.applications.valueobjects.SharedWithProfile;
import com.langa.backend.domainexchange.teams.TeamService;
import com.langa.backend.domainexchange.user.UserAccountService;

@UseCase
public class RevokeSharingUseCase implements IRevokeSharingUseCase, CommandHandler<RevokeSharingApplicationCommand, ApplicationInfo> {

    private final ApplicationRepository applicationRepository;
    private final TeamService teamService;
    private final UserAccountService userAccountService;

    public RevokeSharingUseCase(ApplicationRepository applicationRepository, TeamService teamService, UserAccountService userAccountService) {
        this.applicationRepository = applicationRepository;
        this.teamService = teamService;
        this.userAccountService = userAccountService;
    }

    public void revokeSharing(String appId, String owner, String shareWith, SharedWithProfile profile) {
        final Application app =  applicationRepository.findById(appId)
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));
        app.checkOwnership(owner);

        final ShareWithInfo accountOrTeamInfo = getShareWithInfo(shareWith, profile);

        app.revokeSharing(accountOrTeamInfo.key());

        applicationRepository.save(app);
    }

    private ShareWithInfo getShareWithInfo(String shareWith, SharedWithProfile profile) {
        return SharedWithProfile.USER == profile ? userAccountService.getShareWithInfo(shareWith) : teamService.getShareWithInfo(shareWith);
    }

    @Override
    public ApplicationInfo execute(RevokeSharingApplicationCommand command) {
        final Application app =  applicationRepository.findById(command.appId())
                .orElseThrow(() -> new ApplicationException("Application not found", null, Errors.APPLICATION_NOT_FOUND));
        app.checkOwnership(command.owner());

        final ShareWithInfo accountOrTeamInfo = getShareWithInfo(command.shareWith(), command.profile());

        app.revokeSharing(accountOrTeamInfo.key());

        applicationRepository.save(app);
        return ApplicationInfo.of(app);
    }

    @Override
    public ApplicationInfo handle(RevokeSharingApplicationCommand command) {
        return execute(command);
    }
}
