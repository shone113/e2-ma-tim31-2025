package ftn.project.domain.usecase;

import java.time.LocalDateTime;
import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.domain.entity.TaskInstance;

public class SuccessRateService {
    public static double calculateStageSuccessRate(AppDatabase db, LocalDateTime stageStart, LocalDateTime stageEnd) {
        // uzmi samo one taskove koji su u kvoti
        List<TaskInstance> inQuota = db.taskInstanceRepository()
                .getInQuotaTasksBetween(stageStart, stageEnd);

        int totalTasks = inQuota.size();
        int doneTasks = (int) inQuota.stream()
                .filter(t -> t.getStatus() == TaskInstance.TaskStatusEnum.DONE)
                .count();

        if (totalTasks == 0) return 0.0;
        return (doneTasks * 100.0) / totalTasks;
    }

}
