package ftn.project.domain.usecase;

import ftn.project.domain.repositoryInterface.AllianceInviteRepositoryInterface;

public class DeclineAllianceInviteService {
    private final AllianceInviteRepositoryInterface repo;
    public DeclineAllianceInviteService(AllianceInviteRepositoryInterface repo) {
        this.repo = repo;
    }

    public void execute(String inviteId) throws Exception {
        repo.decline(inviteId);
    }
}
