package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;

import ftn.project.domain.entity.Converters;

@Entity(tableName = "task_instances")
public class TaskInstance {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int taskId; // FK -> Task

    private DifficultyEnum difficultyInstance;
    private ImportanceEnum importanceInstance;

    @TypeConverters({Converters.class})
    private LocalDateTime startExecutionTime;

    @TypeConverters({Converters.class})
    private LocalDateTime endExecutionTime;

    private TaskStatusEnum status;
    private int valueXp;
    private int earnedXp;
    private boolean isWithinQuota;

    public enum DifficultyEnum {
        VERY_EASY(1), EASY(3), HARD(7), EXTREME(20);
        private final int xp;
        DifficultyEnum(int xp) { this.xp = xp; }
        public int getXp() { return xp; }
    }

    public enum ImportanceEnum {
        NORMAL(1), IMPORTANT(3), VERY_IMPORTANT(10), SPECIAL(100);
        private final int xp;
        ImportanceEnum(int xp) { this.xp = xp; }
        public int getXp() { return xp; }
    }

    public enum TaskStatusEnum {
        ACTIVE, DONE, UNFINISHED, PAUSED, CANCELED
    }

    public TaskInstance(int id, int taskId, ImportanceEnum importanceInstance,
                         DifficultyEnum difficultyInstance,
                        LocalDateTime startExecutionTime,
                        LocalDateTime endExecutionTime,
                        TaskStatusEnum status,
                        int valueXp,
                        int earnedXp, boolean isWithinQuota) {
        this.id = id;
        this.taskId = taskId;
        this.importanceInstance = importanceInstance;
        this.difficultyInstance = difficultyInstance;
        this.startExecutionTime = startExecutionTime;
        this.endExecutionTime = endExecutionTime;
        this.status = status;
        this.earnedXp = earnedXp;
        this.isWithinQuota = isWithinQuota;
        this.valueXp = valueXp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public LocalDateTime getStartExecutionTime() {
        return startExecutionTime;
    }

    public void setStartExecutionTime(LocalDateTime startExecutionTime) {
        this.startExecutionTime = startExecutionTime;
    }

    public LocalDateTime getEndExecutionTime() {
        return endExecutionTime;
    }

    public void setEndExecutionTime(LocalDateTime endExecutionTime) {
        this.endExecutionTime = endExecutionTime;
    }

    public TaskStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TaskStatusEnum status) {
        this.status = status;
    }

    public DifficultyEnum getDifficultyInstance() {
        return difficultyInstance;
    }

    public void setDifficultyInstance(DifficultyEnum difficultyInstance) {
        this.difficultyInstance = difficultyInstance;
    }

    public ImportanceEnum getImportanceInstance() {
        return importanceInstance;
    }

    public void setImportanceInstance(ImportanceEnum importanceInstance) {
        this.importanceInstance = importanceInstance;
    }

    public int getEarnedXp() {
        return earnedXp;
    }

    public void setEarnedXp(int earnedXp) {
        this.earnedXp = earnedXp;
    }
    public void setValueXp(int valueXp) {
        this.valueXp = valueXp;
    }
    public int getValueXp() {
        return valueXp;
    }

    public boolean isWithinQuota() {
        return isWithinQuota;
    }

    public void setWithinQuota(boolean withinQuota) {
        isWithinQuota = withinQuota;
    }

    public int computeDifficultyXpForLevel(int levelNumber) {
        if (levelNumber < 0) {
            throw new IllegalArgumentException("levelNumber must be >= 0");
        }
        double xp = this.difficultyInstance.getXp(); // level 0
        for (int i = 0; i < levelNumber; i++) {
            xp = Math.ceil(xp * 1.5);
        }
        return (int) xp;
    }

    public int computeImportanceXpForLevel(int levelNumber) {
        if (levelNumber < 0) {
            throw new IllegalArgumentException("levelNumber must be >= 0");
        }
        double xp = this.importanceInstance.getXp(); // level 0
        for (int i = 0; i < levelNumber; i++) {
            xp = Math.ceil(xp * 1.5); // ZAOKRUŽI posle svakog koraka
        }
        return (int) xp;
    }
}
