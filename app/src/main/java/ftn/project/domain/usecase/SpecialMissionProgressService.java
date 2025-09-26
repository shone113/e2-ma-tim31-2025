package ftn.project.domain.usecase;

import android.content.Context;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.repositoryInterface.AllianceMessageRepositoryInterface;
import ftn.project.domain.repositoryInterface.SpecialMissionProgressRepositoryInterface;
import ftn.project.domain.repositoryInterface.SpecialMissionRepositoryInterface;
import ftn.project.domain.repositoryInterface.TaskInstanceRepositoryInterface;

public class SpecialMissionProgressService {
    private final SpecialMissionRepositoryInterface missionRepository;
    private final SpecialMissionProgressRepositoryInterface missionProgressRepository;
    private final TaskInstanceRepositoryInterface taskInstanceRepository;
    private final AllianceMessageRepositoryInterface allianceMessageRepository;

    public SpecialMissionProgressService(SpecialMissionRepositoryInterface missionRepository,
                                         SpecialMissionProgressRepositoryInterface missionProgressRepository,
                                         TaskInstanceRepositoryInterface taskInstanceRepository, AllianceMessageRepositoryInterface allianceMessageRepository) {
        this.missionRepository = missionRepository;
        this.missionProgressRepository = missionProgressRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.allianceMessageRepository = allianceMessageRepository;
    }
    public boolean isUserInActiveMission(int userId) {
        return missionRepository.getActiveMissionForUser(userId) != null;
    }
    public boolean punchByShopping(int userId) {
        SpecialMission mission = missionRepository.getActiveMissionForUser(userId);
        if (mission == null) {
            return false;
        }

        SpecialMissionProgress smp =
                missionProgressRepository.getProgressByMissionAndUser(mission.getId(), userId);

        if (smp == null) {
            return false;
        }

        return smp.getShopPurchases() < 5;
    }
    public boolean punchByBattle(int userId)
    {
        SpecialMission mission = missionRepository.getActiveMissionForUser(userId);
        if (mission == null) {
            return false;
        }

        SpecialMissionProgress smp =
                missionProgressRepository.getProgressByMissionAndUser(mission.getId(), userId);

        if (smp == null) {
            return false;
        }

        return smp.getRegularBossHits() < 10;
    }

    public int punchByEasyTaskIncrement(int userId, TaskInstance taskInstance) {
        SpecialMission mission = missionRepository.getActiveMissionForUser(userId);
        if (mission == null) {
            return 0;
        }

        SpecialMissionProgress smp =
                missionProgressRepository.getProgressByMissionAndUser(mission.getId(), userId);

        if (smp == null) {
            return 0;
        }

        if (smp.getEasyNormalTasks() >= 10) {
            return 0;
        }

        int increment = 0;

        if (taskInstance.getDifficultyInstance() == TaskInstance.DifficultyEnum.VERY_EASY ||
                taskInstance.getDifficultyInstance() == TaskInstance.DifficultyEnum.EASY) {
            increment++;
        }

        if (taskInstance.getImportanceInstance() == TaskInstance.ImportanceEnum.NORMAL ||
                taskInstance.getImportanceInstance() == TaskInstance.ImportanceEnum.IMPORTANT) {
            increment++;
        }

        if (smp.getEasyNormalTasks() + increment > 10) {
            increment = 10 - smp.getEasyNormalTasks();
        }

        return increment;
    }

    public int punchByHardTaskIncerement(int userId, TaskInstance taskInstance) {
        SpecialMission mission = missionRepository.getActiveMissionForUser(userId);
        if (mission == null) {
            return 0;
        }

        SpecialMissionProgress smp =
                missionProgressRepository.getProgressByMissionAndUser(mission.getId(), userId);

        if (smp == null) {
            return 0;
        }

        if (smp.getEasyNormalTasks() >= 6) {
            return 0;
        }

        int increment = 0;

        if (taskInstance.getDifficultyInstance() == TaskInstance.DifficultyEnum.HARD ||
                taskInstance.getDifficultyInstance() == TaskInstance.DifficultyEnum.EXTREME) {
            increment++;
        }

        if (taskInstance.getImportanceInstance() == TaskInstance.ImportanceEnum.VERY_IMPORTANT ||
                taskInstance.getImportanceInstance() == TaskInstance.ImportanceEnum.SPECIAL) {
            increment++;
        }

        if (smp.getOtherTasks() + increment > 10) {
            increment = 6 - smp.getOtherTasks();
        }

        return increment;
    }

    public int checkNoUnfinishedTasksBonus(int userId, int missionId) {
        SpecialMission mission = missionRepository.getMissionById(missionId);
        if (mission == null) {
            return 0;
        }

        List<TaskInstance> tasks = taskInstanceRepository.getTasksForUserInPeriod(
                userId,
                mission.getStartDate(),
                mission.getEndDate()
        );

        for (TaskInstance ti : tasks) {
            if (ti.getStatus() == TaskInstance.TaskStatusEnum.UNFINISHED
                    || ti.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE) {
                return 0;
            }
        }

        return 10; // ✅ korisnik ispunio uslov
    }


    public SpecialMission getActiveMission(int userId) {
        return missionRepository.getActiveMissionForUser(userId);
    }
    public SpecialMissionProgress getActiveMissionProgress(int missionId, int userId){
        return missionProgressRepository.getProgressByMissionAndUser(missionId, userId);
    }
    public int getTotalProgress(SpecialMission specialMission){
        List <SpecialMissionProgress> allProgress = missionProgressRepository.getAllByMission(specialMission.getId());
        int totalDamage = 0;
        for(SpecialMissionProgress smp : allProgress){
            totalDamage += smp.getTotalDamage();
        }
        return totalDamage;
    }
    public int punchByAllianceMessage(int userId, LocalDate date) {
        SpecialMission mission = missionRepository.getActiveMissionForUser(userId);
        if (mission == null) return 0;

        SpecialMissionProgress smp =
                missionProgressRepository.getProgressByMissionAndUser(mission.getId(), userId);
        if (smp == null) return 0;

        long startOfDay = date.atStartOfDay().toEpochSecond(ZoneOffset.UTC);
        long endOfDay   = date.plusDays(1).atStartOfDay().toEpochSecond(ZoneOffset.UTC) - 1;

        int count = allianceMessageRepository.countMessagesForUserInDay(userId, startOfDay, endOfDay);

        if (count > 1) {
            return 0;
        }


        return 4;
    }

}
