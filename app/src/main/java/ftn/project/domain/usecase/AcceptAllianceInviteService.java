package ftn.project.domain.usecase;

import ftn.project.data.repository.AllianceInviteRepository;
import ftn.project.domain.repositoryInterface.AllianceInviteRepositoryInterface;

public class AcceptAllianceInviteService {
    private final AllianceInviteRepositoryInterface repo;
    public AcceptAllianceInviteService(AllianceInviteRepositoryInterface repo) {
        this.repo = repo;
    }

    public void execute(String inviteId) throws Exception {
        repo.accept(inviteId);
    }
}
