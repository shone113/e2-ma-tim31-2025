package ftn.project.data.repository;

import ftn.project.domain.repositoryInterface.AllianceInviteRepositoryInterface;

public class AllianceInviteRepository implements AllianceInviteRepositoryInterface {
    @Override
    public void accept(String inviteId) throws Exception {
        // konkretna logika, npr. Room query ili API poziv
    }

    @Override
    public void decline(String inviteId) throws Exception {
        // konkretna logika
    }
}
