package ftn.project.domain.entity;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "Alliance",
        indices = { @Index("leader_user_id") },
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "userId",
                        childColumns = "leader_user_id",
                        onDelete = ForeignKey.SET_NULL
                )
        }
)
public class Alliance {
    @PrimaryKey(autoGenerate = true)
    private int allianceId;
    private String name;
    @ColumnInfo(name = "leader_user_id")
    @Nullable
    private int leaderUserId;

    public Alliance() {}

    public int getAllianceId() { return allianceId;}
    public void setAllianceId(int allianceId) { this.allianceId = allianceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getLeaderUserId(){ return leaderUserId; }

    public void setLeaderUserId(int leaderUserId) { this.leaderUserId = leaderUserId; }
}
