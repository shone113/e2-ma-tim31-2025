package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;

@Dao
public interface AllianceInviteRepositoryInterface {

    void accept(String inviteId) throws Exception;
    void decline(String inviteId) throws Exception;
}
