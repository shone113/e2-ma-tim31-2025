package ftn.project.domain.usecase;

public class ComputeLevelStats {
    private static final int XP_REQUIRED = 200;
    private static final int HP_BASE = 200;
    private static final int COINS_BASE = 200;
    private static final double COINS_GROWTH = 1.2;
    private static final int PP_BASE = 40;

    public ComputeLevelStats(){}

    public static int coinsReward(int level){
        if (level < 1) return 0;
        return (int) Math.round(200 * Math.pow(1.2, level - 1));
    }

    public static int coinsPreviousReward(int level){
        if (level <= 1) return 0;
        return (int) Math.round(200 * Math.pow(1.2, level - 2));
    }
}
