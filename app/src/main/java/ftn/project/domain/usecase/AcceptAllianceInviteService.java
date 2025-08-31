package ftn.project.domain.usecase;

import ftn.project.domain.repositoryInterface.AllianceInvitationRepositoryInterface;

public class AcceptAllianceInviteService {
    private final AllianceInvitationRepositoryInterface repo;
    public AcceptAllianceInviteService(AllianceInvitationRepositoryInterface repo) {
        this.repo = repo;
    }

    public void execute(int inviteId) throws Exception {
        repo.accept(inviteId);
    }
}
