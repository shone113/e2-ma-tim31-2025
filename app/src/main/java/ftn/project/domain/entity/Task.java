package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;
import java.time.LocalDateTime;

@Entity(tableName = "tasks")
public class Task {

    // Identifikacija
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int userId;
    private int categoryId;

    // Karakteristike zadatka
    private DifficultyEnum difficulty;
    private ImportanceEnum importance;
    private FrequencyEnum frequency;

    // Parametri ponavljanja
    private int interval;
    private FrequencyUnitEnum frequencyUnit;
    @TypeConverters({Converters.class})
    private LocalDateTime startDate;

    @TypeConverters({Converters.class})
    private LocalDateTime endDate;

    // Osnovni podaci
    private String name;
    private String description;       // može biti null
    @TypeConverters({Converters.class})
    private LocalDateTime executionTime;
    private TaskStatusEnum status;

    // XP vrednost
    @Ignore
    private int valueXP;

    // Enumeracije
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

    public enum FrequencyEnum {
        ONE_TIME, REPEATING
    }

    public enum FrequencyUnitEnum {
        DAY, WEEK
    }

    public enum TaskStatusEnum {
        ACTIVE, DONE, UNFINISHED, PAUSED, CANCELED
    }

    // Konstruktor
    public Task(int id, int userId, int categoryId,
                DifficultyEnum difficulty, ImportanceEnum importance,
                FrequencyEnum frequency, int interval, FrequencyUnitEnum frequencyUnit,
                LocalDateTime startDate, LocalDateTime endDate,
                String name, String description, LocalDateTime executionTime,
                TaskStatusEnum status) {

        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.difficulty = difficulty;
        this.importance = importance;
        this.frequency = frequency;
        this.interval = interval;
        this.frequencyUnit = frequencyUnit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.description = description;
        this.executionTime = executionTime;
        this.status = status;

        // Izračunavanje vrednosti XP
        this.valueXP = difficulty.getXp() + importance.getXp();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public DifficultyEnum getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyEnum difficulty) {
        this.difficulty = difficulty;
    }

    public ImportanceEnum getImportance() {
        return importance;
    }

    public void setImportance(ImportanceEnum importance) {
        this.importance = importance;
    }

    public FrequencyEnum getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyEnum frequency) {
        this.frequency = frequency;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public FrequencyUnitEnum getFrequencyUnit() {
        return frequencyUnit;
    }

    public void setFrequencyUnit(FrequencyUnitEnum frequencyUnit) {
        this.frequencyUnit = frequencyUnit;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public TaskStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TaskStatusEnum status) {
        this.status = status;
    }

    public int getValueXP() { return valueXP; }

    // Ostale gettere/settere dodaj po potrebi
}
