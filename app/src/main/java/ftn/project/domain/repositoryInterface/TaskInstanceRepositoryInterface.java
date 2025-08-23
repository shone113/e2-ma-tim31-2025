package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.time.LocalDateTime;
import java.util.List;

import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;
import ftn.project.domain.entity.TaskInstanceWithTask;

@Dao
public interface TaskInstanceRepositoryInterface {
    @Insert
    void insert(TaskInstance taskInstance);

    @Update
    void update(TaskInstance taskInstance);
    @Query("SELECT * FROM task_instances")
    List<TaskInstance> getAllTasksInstances();

    @Query("UPDATE task_instances SET status = :status WHERE id = :id")
    void updateStatus(int id, TaskInstance.TaskStatusEnum status);

    @Query("DELETE FROM task_instances WHERE taskId = :taskId")
    void deleteByTaskId(int taskId);

    @Query("SELECT EXISTS(SELECT 1 FROM task_instances WHERE taskId = :taskId AND status = 'DONE')")
    boolean hasCompletedInstances(int taskId);

    @Query("DELETE FROM task_instances WHERE taskId = :taskId AND startExecutionTime > :fromDate")
    void deleteFutureInstances(int taskId, LocalDateTime fromDate);

    @Transaction
    @Query("SELECT * FROM task_instances")
    List<TaskInstanceWithTask> getAllTaskInstancesWithTask();

    @Transaction
    @Query("SELECT * FROM task_instances WHERE id = :id")
    TaskInstanceWithTask getTaskInstanceWithTaskById(int id);

    @Transaction
    @Query("SELECT * FROM task_instances " +
            "WHERE startExecutionTime >= :dayStart AND startExecutionTime < :dayEnd")
    List<TaskInstanceWithTask> getInstancesForDay(LocalDateTime dayStart, LocalDateTime dayEnd);
}
