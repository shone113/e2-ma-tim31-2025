package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;

@Dao
public interface TaskInstanceRepositoryInterface {
    @Insert
    void insert(TaskInstance taskInstance);
    @Query("SELECT * FROM task_instances")
    List<TaskInstance> getAllTasksInstances();

    @Query("UPDATE task_instances SET status = :status WHERE id = :id")
    void updateStatus(int id, TaskInstance.TaskStatusEnum status);

    @Transaction
    @Query("SELECT * FROM task_instances")
    List<TaskInstanceWithTask> getAllTaskInstancesWithTask();

    @Transaction
    @Query("SELECT * FROM task_instances WHERE id = :id")
    TaskInstanceWithTask getTaskInstanceWithTaskById(int id);
}
