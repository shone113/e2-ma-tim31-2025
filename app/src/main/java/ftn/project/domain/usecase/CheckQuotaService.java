package ftn.project.domain.usecase;

import java.time.LocalDateTime;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.TaskInstance;

public class CheckQuotaService {

    public static int calculateEarnedXP(TaskInstance taskInstance,int userId, AppDatabase db) {
        int totalXp = 0;
        boolean inQuota = false;

        LocalDateTime ref = taskInstance.getEndExecutionTime();

        int difficultyXp = checkDifficultyQuota(taskInstance,userId, db, ref);
        int importanceXp = checkImportanceQuota(taskInstance,userId, db, ref);

        totalXp = difficultyXp + importanceXp;
        if (totalXp > 0) inQuota = true;

        taskInstance.setEarnedXp(totalXp);
        taskInstance.setWithinQuota(inQuota);

        return totalXp;
    }

    private static int checkDifficultyQuota(TaskInstance ti, int userId, AppDatabase db, LocalDateTime ref) {
        TaskInstance.DifficultyEnum diff = ti.getDifficultyInstance();
        int limit;
        LocalDateTime start, end;

        switch (diff) {
            case VERY_EASY:
            case EASY:
                limit = 5;
                start = ref.toLocalDate().atStartOfDay();
                end = ref.toLocalDate().atTime(23,59,59);
                break;
            case HARD:
                limit = 2;
                start = ref.toLocalDate().atStartOfDay();
                end = ref.toLocalDate().atTime(23,59,59);
                break;
            case EXTREME:
                limit = 1;
                start = ref.toLocalDate().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
                end = ref.toLocalDate().with(java.time.DayOfWeek.SUNDAY).atTime(23,59,59);
                break;
            default:
                return 0;
        }

        int count = db.taskInstanceRepository()
                .countTakenSlotsByDifficulty(userId,diff.name(), start, end);

        return (count < limit) ? diff.getXp() : 0;
    }

    private static int checkImportanceQuota(TaskInstance ti,int userId, AppDatabase db, LocalDateTime ref) {
        TaskInstance.ImportanceEnum imp = ti.getImportanceInstance();
        int limit;
        LocalDateTime start, end;

        switch (imp) {
            case NORMAL:
            case IMPORTANT:
                limit = 5;
                start = ref.toLocalDate().atStartOfDay();
                end = ref.toLocalDate().atTime(23,59,59);
                break;
            case VERY_IMPORTANT:
                limit = 2;
                start = ref.toLocalDate().atStartOfDay();
                end = ref.toLocalDate().atTime(23,59,59);
                break;
            case SPECIAL:
                limit = 1;
                start = ref.toLocalDate().withDayOfMonth(1).atStartOfDay();
                end = ref.toLocalDate().withDayOfMonth(ref.toLocalDate().lengthOfMonth()).atTime(23,59,59);
                break;
            default:
                return 0;
        }

        int count = db.taskInstanceRepository()
                .countTakenSlotsByImportance(userId,imp.name(), start, end);

        return (count < limit) ? imp.getXp() : 0;
    }
}
