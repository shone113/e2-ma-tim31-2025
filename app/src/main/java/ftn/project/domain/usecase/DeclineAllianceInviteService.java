package ftn.project.domain.usecase;

import ftn.project.domain.repositoryInterface.AllianceInvitationRepositoryInterface;

public class DeclineAllianceInviteService {
    private final AllianceInvitationRepositoryInterface repo;
    public DeclineAllianceInviteService(AllianceInvitationRepositoryInterface repo) {
        this.repo = repo;
    }

    public void execute(int inviteId) throws Exception {
        repo.decline(inviteId);
    }
}
