package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "special_mission_progress",
        foreignKeys = @ForeignKey(
                entity = SpecialMission.class,
                parentColumns = "id",
                childColumns = "missionId",
                onDelete = ForeignKey.CASCADE
        )
)
public class SpecialMissionProgress {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private int missionId;
    private int userId;

    private int totalDamage;
    private int shopPurchases;         // max 5
    private int regularBossHits;       // max 10
    private int easyNormalTasks;       // max 10 (lak i normalan računaju se kao duplo)
    private int otherTasks;            // max 6
    private boolean noUnfinished;      // true/false -> 10 HP
    private int allianceMessages;      // broj dana kada je poslao poruku (računa se dnevno, max koliko dana traje misija)

    public SpecialMissionProgress(int id, int missionId, int userId, int totalDamage, int shopPurchases, int regularBossHits, int easyNormalTasks, int otherTasks, boolean noUnfinished, int allianceMessages) {
        this.id = id;
        this.missionId = missionId;
        this.userId = userId;
        this.totalDamage = totalDamage;
        this.shopPurchases = shopPurchases;
        this.regularBossHits = regularBossHits;
        this.easyNormalTasks = easyNormalTasks;
        this.otherTasks = otherTasks;
        this.noUnfinished = noUnfinished;
        this.allianceMessages = allianceMessages;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMissionId() {
        return missionId;
    }

    public void setMissionId(int missionId) {
        this.missionId = missionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTotalDamage() {
        return totalDamage;
    }

    public void setTotalDamage(int totalDamage) {
        this.totalDamage = totalDamage;
    }

    public int getShopPurchases() {
        return shopPurchases;
    }

    public void setShopPurchases(int shopPurchases) {
        this.shopPurchases = shopPurchases;
    }

    public int getRegularBossHits() {
        return regularBossHits;
    }

    public void setRegularBossHits(int regularBossHits) {
        this.regularBossHits = regularBossHits;
    }

    public int getEasyNormalTasks() {
        return easyNormalTasks;
    }

    public void setEasyNormalTasks(int easyNormalTasks) {
        this.easyNormalTasks = easyNormalTasks;
    }

    public int getOtherTasks() {
        return otherTasks;
    }

    public void setOtherTasks(int otherTasks) {
        this.otherTasks = otherTasks;
    }

    public boolean isNoUnfinished() {
        return noUnfinished;
    }

    public void setNoUnfinished(boolean noUnfinished) {
        this.noUnfinished = noUnfinished;
    }

    public int getAllianceMessages() {
        return allianceMessages;
    }

    public void setAllianceMessages(int allianceMessages) {
        this.allianceMessages = allianceMessages;
    }


}