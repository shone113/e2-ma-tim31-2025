package ftn.project.data.dto;

public class LevelDTO {
    public final int levelNumber;
    public final String titleIconKey;
    public final int requiredXP;
    public final int remainingXP;
    public final boolean reachedStatus;

    public LevelDTO(int levelNumber, String titleIconKey, int requiredXP, int remainingXP, boolean reachedStatus){
        this.levelNumber = levelNumber;
        this.titleIconKey = titleIconKey;
        this.requiredXP = requiredXP;
        this.remainingXP = remainingXP;
        this.reachedStatus = reachedStatus;
    }
}
