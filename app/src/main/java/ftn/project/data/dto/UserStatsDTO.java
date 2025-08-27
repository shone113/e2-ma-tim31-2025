package ftn.project.data.dto;

public class UserStatsDTO {
    public final long coins;
    public final long powerPoints;
    public final int level;
    public final int experiencePoints;

    public UserStatsDTO(long coins, long powerPoints, int level, int experiencePoints) {
        this.coins = coins;
        this.powerPoints = powerPoints;
        this.level = level;
        this.experiencePoints = experiencePoints;
    }
}
