package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

@Entity(
        tableName = "special_missions",
        foreignKeys = {
                @ForeignKey(
                        entity = Alliance.class,
                        parentColumns = "allianceId",
                        childColumns = "allianceId",
                        onDelete = ForeignKey.SET_NULL
                )
        })
public class SpecialMission {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int allianceId;
    private int bossHp;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;

    public SpecialMission(int id, int allianceId, int bossHp, LocalDateTime startDate, LocalDateTime endDate, boolean isActive) {
        this.id = id;
        this.allianceId = allianceId;
        this.bossHp = bossHp;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(int allianceId) {
        this.allianceId = allianceId;
    }

    public int getBossHp() {
        return bossHp;
    }

    public void setBossHp(int bossHp) {
        this.bossHp = bossHp;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
