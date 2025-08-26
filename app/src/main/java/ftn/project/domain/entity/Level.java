package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Level {
    @PrimaryKey(autoGenerate = true)
    private int levelId;
    private int levelNumber;
    private int requiredXP;
    private String titleImage;
    public String titleNameKey;
    public String titleIconKey;

    public Level(){}

    public int getLevelNumber(){ return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int getRequiredXP() {
        return requiredXP;
    }

    public void setRequiredXP(int requiredXP) {
        this.requiredXP = requiredXP;
    }

    public String getTitleImage() {
        return titleImage;
    }

    public void setTitleImage(String titleImage) {
        this.titleImage = titleImage;
    }

    public String getTitleNameKey() {
        return titleNameKey;
    }

    public void setTitleNameKey(String titleNameKey) {
        this.titleNameKey = titleNameKey;
    }

    public String getTitleIconKey() {
        return titleIconKey;
    }

    public void setTitleIconKey(String titleIconKey) {
        this.titleIconKey = titleIconKey;
    }
}
