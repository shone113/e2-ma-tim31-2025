package ftn.project.domain.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ftn.project.domain.entity.TaskInstance;

public final class BestStreakStatsService {

    public static class Result {
        public final int currentStreak;
        public final int bestStreak;
        public Result(int current, int best) { this.currentStreak = current; this.bestStreak = best; }
    }

    public static int computeStreaks(List<TaskInstance> instances, ZoneId zone) {
        // 1) Skupi aktivne dane (engagement)
        Set<LocalDate> activeDays = new HashSet<>();
        for (TaskInstance ti : instances) {
            if (ti == null) continue;
            if (ti.getStatus() == TaskInstance.TaskStatusEnum.ACTIVE || ti.getStatus() == TaskInstance.TaskStatusEnum.DONE) continue;

            LocalDateTime ts = (ti.getStartExecutionTime() != null) ? ti.getStartExecutionTime() : ti.getEndExecutionTime();
            if (ts == null) continue;

            LocalDate d = ts.atZone(zone).toLocalDate();
            activeDays.add(d);
        }

        if (activeDays.isEmpty()) return 0;

        // 2) Izračunaj best streak
        List<LocalDate> daysSorted = new ArrayList<>(activeDays);
        Collections.sort(daysSorted);
        int best = 1, curr = 1;
        for (int i = 1; i < daysSorted.size(); i++) {
            LocalDate prev = daysSorted.get(i - 1);
            LocalDate cur  = daysSorted.get(i);
            if (prev.plusDays(1).equals(cur)) {
                curr++;
            } else {
                best = Math.max(best, curr);
                curr = 1;
            }
        }
        return Math.max(best, curr);
    }
}
