package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;

import ftn.project.domain.entity.Converters;

@Entity(tableName = "task_instances")
public class TaskInstance {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int taskId; // FK -> Task

    @TypeConverters({Converters.class})
    private LocalDateTime startExecutionTime;

    @TypeConverters({Converters.class})
    private LocalDateTime endExecutionTime;

    private TaskStatusEnum status;

    public enum TaskStatusEnum {
        ACTIVE, DONE, UNFINISHED, PAUSED, CANCELED
    }

    public TaskInstance(int id, int taskId,
                        LocalDateTime startExecutionTime,
                        LocalDateTime endExecutionTime,
                        TaskStatusEnum status) {
        this.id = id;
        this.taskId = taskId;
        this.startExecutionTime = startExecutionTime;
        this.endExecutionTime = endExecutionTime;
        this.status = status;
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
}
