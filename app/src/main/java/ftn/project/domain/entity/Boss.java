package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bosses")
public class Boss {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String bossImage;
    private int hp;
    private int maxHp;
    private int level;
    private int coinReward;
    private boolean isDefeated;

    public Boss(int id, String bossImage, int hp, int maxHp, int level, int coinReward, boolean isDefeated) {
        this.id = id;
        this.bossImage = bossImage;
        this.hp = hp;
        this.maxHp = maxHp;
        this.level = level;
        this.coinReward = coinReward;
        this.isDefeated = isDefeated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBossImage() {
        return bossImage;
    }

    public void setBossImage(String bossImage) {
        this.bossImage = bossImage;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getCoinReward() {
        return coinReward;
    }

    public void setCoinReward(int coinReward) {
        this.coinReward = coinReward;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public void setDefeated(boolean defeated) {
        isDefeated = defeated;
    }
}
