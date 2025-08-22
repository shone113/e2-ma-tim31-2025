package ftn.project.domain.entity;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

@Entity(
        tableName = "UserEquipment",
        primaryKeys = {"userId", "equipmentId"},
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "userId",      // PROMENI ako je u User-u drugačije ime PK-a
                        childColumns = "userId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Equipment.class,
                        parentColumns = "equipmentId", // PROMENI ako je u Equipment-u drugačije ime PK-a
                        childColumns = "equipmentId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("userId"),
                @Index("equipmentId")
        }
)public class UserEquipment {
    private int equipmentId;
    private int userId;
    private int battleCount;

    public UserEquipment(){}
    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBattleCount() {
        return battleCount;
    }

    public void setBattleCount(int battleCount) {
        this.battleCount = battleCount;
    }

}
