package ftn.project.domain.usecase;

import android.content.Context;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.repositoryInterface.BossRepositoryInterface;

public class BossService {
    private final BossRepositoryInterface bossRepository;

    public BossService(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.bossRepository = db.bossRepository();
    }

    public Boss getOrCreateBossForLevel(int level) {
        Boss boss = bossRepository.getBossByLevel(level);
        if (boss != null) return boss;

        // Ako ne postoji, kreiraj
        int hp = calculateHpForLevel(level);
        int reward = calculateCoinReward(level);

        Boss newBoss = new Boss(
                0,
                "boss_" + level,
                hp,
                hp,
                level,
                reward,
                false
        );

        long id = bossRepository.insert(newBoss);
        newBoss.setId((int) id);

        return newBoss;
    }

    private int calculateHpForLevel(int level) {
        int hp = 200;
        for (int i = 1; i <= level; i++) {
            hp = hp * 2 + hp / 2;
        }
        return hp;
    }

    private int calculateCoinReward(int level) {
        double reward = 200;
        for (int i = 1; i <= level; i++) {
            reward *= 1.2;
        }
        return (int) reward;
    }
}
