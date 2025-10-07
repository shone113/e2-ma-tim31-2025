package ftn.project.domain.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "battles")
public class Battle {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId;
    private int bossId;

    private boolean isVictory;
    private int coinsEarned;
    private int attacksRemaining;

    @Nullable
    private Integer equipmentId;
    private boolean isFinished;

    public Battle(int id, int userId, int bossId, boolean isVictory, int coinsEarned, int attacksRemaining, @Nullable Integer equipmentId, boolean isFinished) {
        this.id = id;
        this.userId = userId;
        this.bossId = bossId;
        this.isVictory = isVictory;
        this.coinsEarned = coinsEarned;
        this.attacksRemaining = attacksRemaining;
        this.equipmentId = equipmentId;
        this.isFinished = isFinished;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBossId() {
        return bossId;
    }

    public void setBossId(int bossId) {
        this.bossId = bossId;
    }

    public boolean isVictory() {
        return isVictory;
    }

    public void setVictory(boolean victory) {
        isVictory = victory;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }

    public int getAttacksRemaining() {
        return attacksRemaining;
    }

    public void setAttacksRemaining(int attacksRemaining) {
        this.attacksRemaining = attacksRemaining;
    }

    @Nullable
    public Integer getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(@Nullable Integer equipmentId) {
        this.equipmentId = equipmentId;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void setFinished(boolean finished) {
        isFinished = finished;
    }
}

