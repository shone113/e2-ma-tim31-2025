package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ftn.project.domain.entity.AllianceInvitation;
import ftn.project.domain.entity.InvitationStatus;

@Dao
public interface AllianceInvitationRepositoryInterface {

    @Transaction
    default void accept(int invitationId) {
    }

    @Transaction
    default void decline(int invitationId) {}

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(AllianceInvitation inv);

    @Query("SELECT * FROM AllianceInvitation WHERE inviterUserId = :inviterUserId")
    List<AllianceInvitation> findAllByInviter(int inviterUserId);
}
