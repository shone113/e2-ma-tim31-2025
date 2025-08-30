package ftn.project.di;

import ftn.project.data.repository.AllianceInviteRepository;
import ftn.project.domain.usecase.AcceptAllianceInviteService;
import ftn.project.domain.usecase.DeclineAllianceInviteService;

public final class Deps {
    private Deps() {}

    public static AcceptAllianceInviteService acceptInviteUseCase() {
        return new AcceptAllianceInviteService(new AllianceInviteRepository());
    }

    public static DeclineAllianceInviteService declineInviteUseCase() {
        return new DeclineAllianceInviteService(new AllianceInviteRepository());
    }
}
