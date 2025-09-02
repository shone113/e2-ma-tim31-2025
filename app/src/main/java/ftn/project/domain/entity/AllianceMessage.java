package ftn.project.domain.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "AllianceMessage",
        indices = {@Index("allianceId"), @Index("sentAt")})
public class AllianceMessage {
    @PrimaryKey
    @NonNull
    private int messageId;
    private int allianceId;
    @NonNull private String creatorUsername;
    @NonNull private String content;
    private long sentAt;

    @NonNull
    public int getMessageId() { return messageId; }
    public void setMessageId(@NonNull int messageId) { this.messageId = messageId; }
    public int getAllianceId() { return allianceId; }
    public void setAllianceId(int allianceId) { this.allianceId = allianceId; }
    @NonNull public String getCreatorUsername() { return creatorUsername; }
    public void setCreatorUsername(@NonNull String creatorUsername) { this.creatorUsername = creatorUsername; }

    @NonNull public String getContent() { return content; }
    public void setContent(@NonNull String content) { this.content = content; }
    public long getSentAt() { return sentAt; }
    public void setSentAt(long sentAt) { this.sentAt = sentAt; }
}
