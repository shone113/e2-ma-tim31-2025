package ftn.project.domain.entity;

import androidx.room.TypeConverter;

import java.time.LocalDateTime;

public class Converters {
    @TypeConverter
    public static LocalDateTime fromTimestamp(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }

    @TypeConverter
    public static String dateToTimestamp(LocalDateTime date) {
        return date == null ? null : date.toString();
    }

    // Enum konverteri
    @TypeConverter
    public static String fromDifficulty(Task.DifficultyEnum difficulty) {
        return difficulty == null ? null : difficulty.name();
    }

    @TypeConverter
    public static Task.DifficultyEnum toDifficulty(String value) {
        return value == null ? null : Task.DifficultyEnum.valueOf(value);
    }

    @TypeConverter
    public static String fromImportance(Task.ImportanceEnum importance) {
        return importance == null ? null : importance.name();
    }

    @TypeConverter
    public static Task.ImportanceEnum toImportance(String value) {
        return value == null ? null : Task.ImportanceEnum.valueOf(value);
    }

    @TypeConverter
    public static String fromFrequency(Task.FrequencyEnum frequency) {
        return frequency == null ? null : frequency.name();
    }

    @TypeConverter
    public static Task.FrequencyEnum toFrequency(String value) {
        return value == null ? null : Task.FrequencyEnum.valueOf(value);
    }

    @TypeConverter
    public static String fromFrequencyUnit(Task.FrequencyUnitEnum unit) {
        return unit == null ? null : unit.name();
    }

    @TypeConverter
    public static Task.FrequencyUnitEnum toFrequencyUnit(String value) {
        return value == null ? null : Task.FrequencyUnitEnum.valueOf(value);
    }

    @TypeConverter
    public static String fromStatus(Task.TaskStatusEnum status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static Task.TaskStatusEnum toStatus(String value) {
        return value == null ? null : Task.TaskStatusEnum.valueOf(value);
    }
}
