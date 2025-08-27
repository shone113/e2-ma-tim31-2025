package ftn.project.domain.usecase;

import android.content.Context;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Battle;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.entity.User;

public class BattleStartService {
    private final AppDatabase db;
    private final BossService bossService;

    public BattleStartService(Context context) {
        this.db = AppDatabase.getInstance(context);
        this.bossService = new BossService(context);
    }

    public BattleStartResult startNewBattle(User currentUser) {
        int currentLevel = currentUser.getLevel();

        // 1. Nađi ili kreiraj bossa
        Boss boss = bossService.getOrCreateBossForLevel(currentLevel);

        // 2. Kreiraj battle
        Battle battle = new Battle(
                0,
                currentUser.getUserId(),
                boss.getId(),
                false,
                0,
                5,
                null,
                false
        );
        long battleId = db.battleRepository().insert(battle);

        // 3. Izračunaj hitChance
        LocalDate startDate = currentUser.getNewLevelTime().toLocalDate();
        LocalDate endDate = LocalDate.of(2025, 8, 30);

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            QuotaFinalizer.finalizeDayQuota(db, current);
            current = current.plusDays(1);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        double hitChance = SuccessRateService.calculateStageSuccessRate(db, startDateTime, endDateTime);
        int hitChanceInt = (int) Math.round(hitChance);

        return new BattleStartResult((int) battleId, hitChanceInt);
    }

    public static class BattleStartResult {
        public final int battleId;
        public final int hitChance;

        public BattleStartResult(int battleId, int hitChance) {
            this.battleId = battleId;
            this.hitChance = hitChance;
        }
    }
}
