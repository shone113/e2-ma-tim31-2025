package ftn.project.domain.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AllianceInvitation {
    @PrimaryKey(autoGenerate = true)
    public int invitationId;
    public int allianceId;
    public int inviterUserId;
    public int inviteeUserId;
    @NonNull
    public InvitationStatus status;

    public int getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(int invitationId) {
        this.invitationId = invitationId;
    }

    public int getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(int allianceId) {
        this.allianceId = allianceId;
    }

    public int getInviterUserId() {
        return inviterUserId;
    }

    public void setInviterUserId(int inviterUserId) {
        this.inviterUserId = inviterUserId;
    }

    public int getInviteeUserId() {
        return inviteeUserId;
    }

    public void setInviteeUserId(int inviteeUserId) {
        this.inviteeUserId = inviteeUserId;
    }

    @NonNull
    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull InvitationStatus status) {
        this.status = status;
    }

}
