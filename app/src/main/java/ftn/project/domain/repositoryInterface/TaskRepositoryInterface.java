package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ftn.project.domain.entity.Task;

@Dao
public interface TaskRepositoryInterface {
    @Insert
    void insert(Task task);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Query("UPDATE tasks SET status = :status WHERE id = :taskId")
    void updateTaskStatus(int taskId, Task.TaskStatusEnum status);

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    Task getTaskById(int id);
}
