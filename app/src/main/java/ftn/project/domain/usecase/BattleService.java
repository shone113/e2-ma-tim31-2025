package ftn.project.domain.usecase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.ActiveType;
import ftn.project.domain.entity.Battle;
import ftn.project.domain.entity.Boss;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.EquipmentType;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;

//MORAM UPDATOVATI COINSE KOD OVOG USERA
public class BattleService {

    private final AppDatabase db;
    private final Context context;
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public BattleService(Context context) {
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    public interface BattleResultListener {
        void onBattleFinished(Battle battle, int coins, String equipmentIcon);
        void onAttackResult(boolean hit, Battle battle, Boss boss);
    }

    private BattleResultListener resultListener;

    public void setBattleResultListener(BattleResultListener listener) {
        this.resultListener = listener;
    }
    /**
     * Izvrši napad nad bossom (u background niti)
     */
    public void performAttack(Battle battle, Boss boss, int userPp, int hitChance, Runnable onUiUpdate) {
        executor.execute(() -> {
            if (battle.getAttacksRemaining() <= 0 || boss.isDefeated()) {
                return;
            }

            // smanji broj napada
            battle.setAttacksRemaining(battle.getAttacksRemaining() - 1);
            boolean hit = false;
            int roll = random.nextInt(100);
            if (roll < hitChance) {
                // pogodak
                hit = true;
                boss.setHp(boss.getHp() - userPp);
                if (boss.getHp() < 0) boss.setHp(0);

                if (boss.getHp() == 0) {
                    boss.setDefeated(true);
                    finishBattle(battle, boss, true);
                }
            } /*else {
                runOnUi(() -> Toast.makeText(context, "Promašaj!", Toast.LENGTH_SHORT).show());
            }*/

            if (battle.getAttacksRemaining() == 0 && !boss.isDefeated()) {
                finishBattle(battle, boss, false);
            }

            // update u bazi
            db.battleRepository().update(battle);
            db.bossRepository().update(boss);

            if (resultListener != null) {
                boolean finalHit = hit;
                runOnUi(() -> resultListener.onAttackResult(finalHit, battle, boss));
            }

            // pozovi callback da Activity može da osveži UI
            if (onUiUpdate != null) {
                runOnUi(onUiUpdate);
            }
        });
    }

    /**
     * Završava bitku – pobeda ili poraz
     */
    private Equipment getRandomEquipment() {
        int roll = random.nextInt(100);
        EquipmentType type = (roll < 95) ? EquipmentType.ARMOR : EquipmentType.WEAPON; // 95% armor, 5% weapon

        // Izvuci listu iz baze
        List<Equipment> list = db.equipmentRepository().getAllByType(type);

        if (list == null || list.isEmpty()) return null;

        // Izaberi random item iz liste
        return list.get(random.nextInt(list.size()));
    }
    private String tryGiveEquipmentReward(Battle battle, int chance) {
        int roll = random.nextInt(100);
        if (roll < chance) {
            Equipment reward = getRandomEquipment();
            if (reward != null) {
                UserEquipment ue = new UserEquipment();
                ue.setUserId(battle.getUserId());
                ue.setEquipmentId(reward.getEquipmentId());
                if(reward.getActiveType() == ActiveType.ONE_USE)
                    ue.setBattleCount(1);
                else
                    ue.setBattleCount(2);
                ue.setActive(false);
                db.userEquipmentRepository().add(ue);
                return reward.getName(); // ili getName() ako nemaš posebno polje
            }
        }
        return null;
    }


    private void finishBattle(Battle battle, Boss boss, boolean victory) {
        battle.setFinished(true);
        battle.setVictory(victory);
        int chance = 100;
        int coins = 0;
        String equipmentName = null;

        if (victory) {
            coins = boss.getCoinReward();
            battle.setCoinsEarned(coins);
            equipmentName = tryGiveEquipmentReward(battle, chance);
        } else {
            if (boss.getHp() < boss.getMaxHp() / 2) {
                coins = boss.getCoinReward() / 2;
                battle.setCoinsEarned(coins);
                equipmentName = tryGiveEquipmentReward(battle, chance / 2);
            } else {
                battle.setCoinsEarned(0);
            }
        }

        db.battleRepository().update(battle);
        db.bossRepository().update(boss);
        db.userRepository().addCoins(battle.getUserId(), coins);

        // ✅ umesto samo Toast → javimo activity-ju
        if (resultListener != null) {
            final String finalEquipment = equipmentName; // ✅ sad je final
            final int finalCoins = coins;
            runOnUi(() -> resultListener.onBattleFinished(battle, finalCoins, finalEquipment));
        }
    }




    private void runOnUi(Runnable runnable) {
        mainHandler.post(runnable);
    }
}
