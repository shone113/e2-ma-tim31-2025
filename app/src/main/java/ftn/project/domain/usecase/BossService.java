package ftn.project.domain.usecase;

import android.content.Context;

import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.repositoryInterface.BossRepositoryInterface;

public class BossService {
    private final BossRepositoryInterface bossRepository;

    public BossService(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.bossRepository = db.bossRepository();
    }

    public Boss getOrCreateBossForLevel(int currentLevel) {
        // 1. Da li ima neporaženih bossova sa manjim levelom?
        List<Boss> unfinished = bossRepository.getAllUnfinishedBeforeLevel(currentLevel);
        if (!unfinished.isEmpty()) {
            // Vrati prvog nepobeđenog
            return unfinished.get(0);
        }

        // 2. Ako nema nepobeđenih, ide boss za trenutni level
        Boss boss = bossRepository.getBossByLevel(currentLevel);
        if (boss != null) return boss;

        // 3. Ako ne postoji, kreiraj
        int hp = calculateHpForLevel(currentLevel);
        int reward = calculateCoinReward(currentLevel);

        Boss newBoss = new Boss(
                0,
                "boss_" + currentLevel,
                hp,
                hp,
                currentLevel,
                reward,
                false
        );

        long id = bossRepository.insert(newBoss);
        newBoss.setId((int) id);

        return newBoss;
    }
    public void ensureBossForLevel(int currentLevel) {
        Boss boss = bossRepository.getBossByLevel(currentLevel);
        if (boss == null) {
            int hp = calculateHpForLevel(currentLevel);
            int reward = calculateCoinReward(currentLevel);

            Boss newBoss = new Boss(
                    0,
                    "boss_" + currentLevel,
                    hp,
                    hp,
                    currentLevel,
                    reward,
                    false
            );

            long id = bossRepository.insert(newBoss);
            newBoss.setId((int) id);
        }
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
