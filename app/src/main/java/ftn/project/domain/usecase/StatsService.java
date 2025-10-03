package ftn.project.domain.usecase;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.CategoryBarData;
import ftn.project.domain.entity.Category;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;
import ftn.project.domain.repositoryInterface.CategoryRepositoryInterface;
import ftn.project.domain.repositoryInterface.SpecialMissionProgressRepositoryInterface;
import ftn.project.domain.repositoryInterface.TaskInstanceRepositoryInterface;

public class StatsService {
    private final int userId;
    private TaskInstanceRepositoryInterface taskInstanceRepository;
    private CategoryRepositoryInterface categoryRepository;
    private SpecialMissionProgressRepositoryInterface specialMissionProgressRepository;

    public StatsService(AppDatabase db, int userId){
        taskInstanceRepository = db.taskInstanceRepository();
        categoryRepository = db.categoryRepository();
        specialMissionProgressRepository = db.specialMissionProgressRepository();
        this.userId = userId;
    }

    public CategoryBarData getFinishedTasksByCategory(int userId){
        List<TaskInstanceWithTask> tasksWithInstances = taskInstanceRepository.getAllTaskInstancesWithTask(userId);

        Map<Integer, Integer> countByCatId = new HashMap<>();
        for (TaskInstanceWithTask row : tasksWithInstances) {
            if (row == null || row.taskInstance == null) continue;
            if (row.taskInstance.getStatus() == TaskInstance.TaskStatusEnum.DONE) {
                int categoryId = (row.task != null) ? row.task.getCategoryId() : -1;
                countByCatId.put(categoryId, countByCatId.getOrDefault(categoryId, 0) + 1);
            }
        }

        // 2) Sastavi liste: naziv, vrednost, boja
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        for (Map.Entry<Integer, Integer> e : countByCatId.entrySet()) {
            Integer catId = e.getKey();
            int count = e.getValue();

            String name = "Uncategorized";
            int argbColor = 0xFF9E9E9E; // default siva

            if (catId != null && catId != -1) {
                Category c = categoryRepository.getById(catId); // prilagodi tvom tipu/DAO-u
                if (c != null) {
                    if (c.getName() != null && !c.getName().trim().isEmpty()) {
                        name = c.getName();
                    }
                    int stored = c.getColor(); // pretpostavka: RGB ili ARGB int
                    // Ako je RGB (0xRRGGBB) bez alfe -> dodaj punu opacnost
                    argbColor = ((stored & 0xFF000000) == 0) ? (0xFF000000 | stored) : stored;
                }
            }

            labels.add(name);
            values.add(count);
            colors.add(argbColor);
        }

        return new CategoryBarData(labels, values, colors);
    }

    public Map<String, Integer> getTaskStatusCounts(int userId){
        List<TaskInstanceWithTask> tasksWithInstances = taskInstanceRepository.getAllTaskInstancesWithTask(userId);

        int created  = tasksWithInstances.size();
        int done     = 0;
        int unfinished   = 0;
        int canceled = 0;

        for (TaskInstanceWithTask row : tasksWithInstances) {
            if (row == null || row.taskInstance == null) continue;
            TaskInstance.TaskStatusEnum st = row.taskInstance.getStatus();
            if (st == TaskInstance.TaskStatusEnum.DONE) {
                done++;
            } else if (st == TaskInstance.TaskStatusEnum.CANCELED) {
                canceled++;
            } else {
                // sve ostalo tretiramo kao "Undone"
                unfinished++;
            }
        }

        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("Created",  created);
        result.put("Done",     done);
        result.put("Unfinished",   unfinished);
        result.put("Canceled", canceled);
        return result;
    }

    public Map<String, Float> getAvgDifficultyPerDay(int userId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM");
        LinkedHashMap<String, Float> out = new LinkedHashMap<>(); // zadržava redosled

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd   = dayStart.plusDays(1);

            List<TaskInstanceWithTask> rows =
                    taskInstanceRepository.getInstancesForDay(userId, dayStart, dayEnd);

            int sum = 0, cnt = 0;
            for (TaskInstanceWithTask r : rows) {
                if (r == null || r.taskInstance == null) continue;
                if (r.taskInstance.getStatus() != TaskInstance.TaskStatusEnum.DONE) continue;

                int rate = difficultyToRating(r.taskInstance.getDifficultyInstance()); // 1..4
                if (rate == 0) continue;
                sum += rate; cnt++;
            }

            String label = fmt.format(day);
            out.put(label, cnt > 0 ? (sum / (float) cnt) : Float.NaN); // ili 0f ako želiš nulu
        }
        return out;
    }
    private static int difficultyToRating(TaskInstance.DifficultyEnum d) {
        switch (d) {
            case VERY_EASY: return 1;
            case EASY:      return 2;
            case HARD:      return 3;
            case EXTREME:   return 4;
            default:        return 0;
        }
    }


    public Map<String, Integer> getXpLast7Days(int userId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM");
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd   = dayStart.plusDays(1);

            List<TaskInstanceWithTask> rows =
                    taskInstanceRepository.getInstancesForDay(userId, dayStart, dayEnd);

            int xpSum = 0;
            for (TaskInstanceWithTask r : rows) {
                if (r == null || r.taskInstance == null) continue;
                if (r.taskInstance.getStatus() != TaskInstance.TaskStatusEnum.DONE) continue;

                xpSum += difficultyToXp(r.taskInstance.getDifficultyInstance());
            }

            out.put(fmt.format(day), xpSum);
        }
        return out;
    }

    private static int difficultyToXp(TaskInstance.DifficultyEnum d) {
        if (d == null) return 0;
        switch (d) {
            case VERY_EASY: return 1;
            case EASY:      return 3;
            case HARD:      return 7;
            case EXTREME:   return 20;
            default:        return 0;
        }
    }
    public int getBestStreakDays(){
        List<TaskInstanceWithTask> tasksWithInstances = taskInstanceRepository.getAllTaskInstancesWithTaskSorted(userId);

        int best = 0, streak = 0;

        LocalDate currentDay = null;
        boolean anyTaskToday = false;
        boolean anyNonDoneToday = false;

        for (TaskInstanceWithTask row : tasksWithInstances) {
            if (row == null || row.taskInstance == null) continue;

            LocalDate day = row.taskInstance.getEndExecutionTime().toLocalDate();

            // prelaz na novi dan -> zatvori prethodni
            if (currentDay != null && !day.equals(currentDay)) {
                if (anyTaskToday) {
                    if (!anyNonDoneToday) {
                        streak += 1;
                    } else {
                        best = Math.max(best, streak);
                        streak = 0;
                    }
                }
                anyTaskToday = false;
                anyNonDoneToday = false;
            }

            currentDay = day;
            anyTaskToday = true;

            TaskInstance.TaskStatusEnum s = row.taskInstance.getStatus();
            if (s != TaskInstance.TaskStatusEnum.DONE) {
                anyNonDoneToday = true;
            }
        }

        if (currentDay != null && anyTaskToday) {
            if (!anyNonDoneToday) {
                streak += 1;
            } else {
                best = Math.max(best, streak);
                streak = 0;
            }
        }

        return Math.max(best, streak);
    }

    public Map<String, Integer> getSpecialMissionStats(){
        List<SpecialMissionProgress> missions = specialMissionProgressRepository.getAllProgressForUser(userId);
        int unfinished = 0;
        for(SpecialMissionProgress mission: missions){
            if(mission.isNoUnfinished()){
                unfinished++;
            }
        }
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("Finished",  unfinished);
        result.put("Unfinished", missions.size() - unfinished);

        return result;
    }

    public Set<LocalDate> getActivityTimestampsFromTasks() {
        List<TaskInstanceWithTask> tasksWithInstances = taskInstanceRepository.getAllTaskInstancesWithTask(userId);
        Set<LocalDate> days = new HashSet<>();

        for (TaskInstanceWithTask row : tasksWithInstances) {
            if (row == null || row.taskInstance == null) continue;

            // Dan na koji se instanca odnosi (prilagodi ako je drugi tip timestamp-a)
            LocalDate day = row.taskInstance.getEndExecutionTime().toLocalDate();

            TaskInstance.TaskStatusEnum s = row.taskInstance.getStatus();
            if (s == TaskInstance.TaskStatusEnum.DONE
                    || s == TaskInstance.TaskStatusEnum.UNFINISHED
                    || s == TaskInstance.TaskStatusEnum.CANCELED) {
                days.add(day);
            }
        }
        return days;
    }

    public int getBestActivityStreak(){
        List<TaskInstance> tasksWithInstances = taskInstanceRepository.getAllTaskInstances(userId);

        BestStreakStatsService bestStreakStatsService = new BestStreakStatsService();

        return  bestStreakStatsService.computeStreaks(tasksWithInstances, ZoneId.systemDefault());

    }
}

