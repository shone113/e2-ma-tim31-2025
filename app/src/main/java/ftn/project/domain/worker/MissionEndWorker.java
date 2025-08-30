package ftn.project.domain.worker;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.domain.usecase.BossService;
import ftn.project.domain.usecase.EquipmentService;
import ftn.project.domain.usecase.SpecialMissionProgressService;

public class MissionEndWorker extends Worker {

    public MissionEndWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    private EquipmentService equipmentService;
    private BossService bossService;
    @NonNull
    @Override
    public Result doWork() {
        int missionId = getInputData().getInt("missionId", -1);
        if (missionId == -1) {
            return Result.failure();
        }

        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        equipmentService = new EquipmentService(db.equipmentRepository());
        bossService = new BossService(db.bossRepository());
        SpecialMissionProgressService smps = new SpecialMissionProgressService(
                db.specialMissionRepository(),
                db.specialMissionProgressRepository(),
                db.taskInstanceRepository()
        );

        // Uzimamo aktivnu misiju
        SpecialMission mission = db.specialMissionRepository().getMissionById(missionId);
        if (mission == null) {
            return Result.failure();
        }

        // Svi učesnici
        List<SpecialMissionProgress> progresses =
                db.specialMissionProgressRepository().getAllProgressForMission(missionId);
        int totalBossDamage = 0;

        for (SpecialMissionProgress smp : progresses) {
            int userId = smp.getUserId();

            int bonus = smps.checkNoUnfinishedTasksBonus(userId, missionId);
            if (bonus > 0) {
                // update za usera
                smp.setTotalDamage(smp.getTotalDamage() + bonus);
                db.specialMissionProgressRepository().update(smp);

                // Bossu skidamo zbir
                totalBossDamage += bonus;
            }
        }

        if (totalBossDamage > 0) {
            mission.setBossHp(mission.getBossHp() - totalBossDamage);
            db.specialMissionRepository().update(mission);
        }
        mission.setActive(false);
        db.specialMissionRepository().update(mission);
        if (mission.getBossHp() <= 0){
            List<SpecialMissionProgress> allProgress =
                    db.specialMissionProgressRepository().getAllProgressForMission(mission.getId());
            for (SpecialMissionProgress smp : allProgress){
                int userId = smp.getUserId();
                User user = db.userRepository().getById(userId);
                Equipment potion = equipmentService.generateRandomPotion();
                Equipment armor = equipmentService.generateRandomClothes();
                int prize = bossService.calculateCoinReward(user.getLevel()) / 2;

                UserEquipment potionReward = new UserEquipment();
                potionReward.setActive(false);
                potionReward.setBattleCount(0);
                potionReward.setUserId(user.getUserId());
                potionReward.setEquipmentId(potion.getEquipmentId());
                db.userEquipmentRepository().add(potionReward);

                UserEquipment armorReward = new UserEquipment();
                armorReward.setActive(false);
                armorReward.setBattleCount(0);
                armorReward.setUserId(user.getUserId());
                armorReward.setEquipmentId(armor.getEquipmentId());
                db.userEquipmentRepository().add(armorReward);

                db.userRepository().addCoins(user.getUserId(),prize);
            }
        }


        return Result.success();
    }
}
