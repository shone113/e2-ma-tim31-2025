package ftn.project.domain.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.TaskInstance;

public class QuotaFinalizer {

    public static void finalizeDayQuota(AppDatabase db, int userId, LocalDate day) {
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end   = day.atTime(23,59,59);

        // ==== Kvote po TEŽINI ====
        int usedVeryEasy = db.taskInstanceRepository()
                .countTakenSlotsByDifficulty(userId, TaskInstance.DifficultyEnum.VERY_EASY.name(), start, end);
        int leftVeryEasy = 5 - usedVeryEasy;

        int usedEasy = db.taskInstanceRepository()
                .countTakenSlotsByDifficulty(userId, TaskInstance.DifficultyEnum.EASY.name(), start, end);
        int leftEasy = 5 - usedEasy;

        int usedHard = db.taskInstanceRepository()
                .countTakenSlotsByDifficulty(userId, TaskInstance.DifficultyEnum.HARD.name(), start, end);
        int leftHard = 2 - usedHard;

        // Nedeljna kvota za EXTREME
        LocalDate weekStart = day.with(java.time.DayOfWeek.MONDAY);
        LocalDate weekEnd = day.with(java.time.DayOfWeek.SUNDAY);
        LocalDateTime weekStartDt = weekStart.atStartOfDay();
        LocalDateTime weekEndDt = weekEnd.atTime(23,59,59);

        int usedExtreme = db.taskInstanceRepository()
                .countTakenSlotsByDifficulty(userId, TaskInstance.DifficultyEnum.EXTREME.name(), weekStartDt, weekEndDt);
        int leftExtreme = 1 - usedExtreme;

        // ==== Kvote po BITNOSTI ====
        int usedNormal = db.taskInstanceRepository()
                .countTakenSlotsByImportance(userId, TaskInstance.ImportanceEnum.NORMAL.name(), start, end);
        int leftNormal = 5 - usedNormal;

        int usedImportant = db.taskInstanceRepository()
                .countTakenSlotsByImportance(userId, TaskInstance.ImportanceEnum.IMPORTANT.name(), start, end);
        int leftImportant = 5 - usedImportant;

        int usedVeryImportant = db.taskInstanceRepository()
                .countTakenSlotsByImportance(userId, TaskInstance.ImportanceEnum.VERY_IMPORTANT.name(), start, end);
        int leftVeryImportant = 2 - usedVeryImportant;

        // Mesečna kvota za SPECIAL
        LocalDate monthStart = day.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate monthEnd = day.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime monthEndDt = monthEnd.atTime(23,59,59);

        int usedSpecial = db.taskInstanceRepository()
                .countTakenSlotsByImportance(userId, TaskInstance.ImportanceEnum.SPECIAL.name(), monthStartDt, monthEndDt);
        int leftSpecial = 1 - usedSpecial;


        // ==== Preostale aktivne / nerešene ====
        List<TaskInstance> pending = db.taskInstanceRepository()
                .getActiveOrUnfinishedForDayOrdered(userId, start, end);

        for (TaskInstance ti : pending) {
            boolean canDiff = false, canImp = false;

            // --- Težina ---
            switch (ti.getDifficultyInstance()) {
                case VERY_EASY: if (leftVeryEasy > 0) { canDiff = true; leftVeryEasy--; } break;
                case EASY: if (leftEasy > 0) { canDiff = true; leftEasy--; } break;
                case HARD: if (leftHard > 0) { canDiff = true; leftHard--; } break;
                case EXTREME: if (leftExtreme > 0) { canDiff = true; leftExtreme--; } break;
            }

            // --- Bitnost ---
            switch (ti.getImportanceInstance()) {
                case NORMAL: if (leftNormal > 0) { canImp = true; leftNormal--; } break;
                case IMPORTANT: if (leftImportant > 0) { canImp = true; leftImportant--; } break;
                case VERY_IMPORTANT: if (leftVeryImportant > 0) { canImp = true; leftVeryImportant--; } break;
                case SPECIAL: if (leftSpecial > 0) { canImp = true; leftSpecial--; } break;
            }

            if (canDiff || canImp) {
                ti.setWithinQuota(true);
                db.taskInstanceRepository().updateWithinQuota(ti.getId(), true);
            } else {
                ti.setWithinQuota(false);
                db.taskInstanceRepository().updateWithinQuota(ti.getId(), false);
            }
        }
    }
}
