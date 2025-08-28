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

    private FrequencyEnum frequency;

    // Parametri ponavljanja
    private int interval; // npr. na 2 dana
    private FrequencyUnitEnum frequencyUnit; // DAN / NEDELJA
    @TypeConverters({Converters.class})
    private LocalDateTime startDate;

    @TypeConverters({Converters.class})
    private LocalDateTime endDate;

    // Osnovni podaci
    private String name;
    private String description;  // može biti null

    // XP vrednost
    @Ignore
    private int valueXP;


    public enum FrequencyEnum { ONE_TIME, REPEATING }
    public enum FrequencyUnitEnum { DAY, WEEK }

    // Konstruktor
    public Task(int id, int userId, int categoryId,
                FrequencyEnum frequency, int interval, FrequencyUnitEnum frequencyUnit,
                LocalDateTime startDate, LocalDateTime endDate,
                String name, String description) {

        this.id = id;
        this.userId = userId;
        this.categoryId = categoryId;
        this.frequency = frequency;
        this.interval = interval;
        this.frequencyUnit = frequencyUnit;
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.description = description;
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

    // Ostale gettere/settere dodaj po potrebi
}
