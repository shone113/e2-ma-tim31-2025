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

    @Query("UPDATE task_instances SET earnedXp = :earnedXp WHERE id = :id")
    void updateEarnedXp(int id, int earnedXp);

    @Query("DELETE FROM task_instances WHERE taskId = :taskId")
    void deleteByTaskId(int taskId);

    @Query("SELECT EXISTS( " +
            "SELECT 1 FROM task_instances " +
            "WHERE taskId = :taskId " +
            "AND (status != 'ACTIVE' OR startExecutionTime < :now)" +
            ")")
    boolean hasLockedInstances(int taskId, LocalDateTime now);

    @Query("DELETE FROM task_instances WHERE taskId = :taskId AND startExecutionTime > :fromDate")
    void deleteFutureInstances(int taskId, LocalDateTime fromDate);

    @Transaction
    @Query("SELECT * FROM task_instances")
    List<TaskInstanceWithTask> getAllTaskInstancesWithTask();

    @Transaction
    @Query("SELECT * FROM task_instances WHERE endExecutionTime > :now")
    List<TaskInstance> getActiveInstances(LocalDateTime now);



    @Transaction
    @Query("SELECT * FROM task_instances WHERE id = :id")
    TaskInstanceWithTask getTaskInstanceWithTaskById(int id);
    @Transaction
    @Query("SELECT * FROM task_instances WHERE taskId = :id AND startExecutionTime >= :now")
    List<TaskInstanceWithTask> getFutureTaskInstancesWithTaskById(int id, LocalDateTime now);


    @Transaction
    @Query("SELECT * FROM task_instances " +
            "WHERE startExecutionTime >= :dayStart AND startExecutionTime < :dayEnd")
    List<TaskInstanceWithTask> getInstancesForDay(LocalDateTime dayStart, LocalDateTime dayEnd);

    // Broj završenih po težini
    @Query("SELECT COUNT(*) FROM task_instances " +
            "WHERE difficultyInstance = :diff " +
            "AND (status = 'DONE' OR isWithinQuota = 1) " +
            "AND endExecutionTime BETWEEN :start AND :end")
    int countTakenSlotsByDifficulty(String diff, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(*) FROM task_instances " +
            "WHERE importanceInstance = :imp " +
            "AND (status = 'DONE' OR isWithinQuota = 1) " +
            "AND endExecutionTime BETWEEN :start AND :end")
    int countTakenSlotsByImportance(String imp, LocalDateTime start, LocalDateTime end);

    // Vrati sve koji su aktivni ili unfinished za dan
    @Query("SELECT * FROM task_instances " +
            "WHERE (status = 'ACTIVE' OR status = 'UNFINISHED') " +
            "AND startExecutionTime BETWEEN :start AND :end " +
            "ORDER BY startExecutionTime ASC")
    List<TaskInstance> getActiveOrUnfinishedForDayOrdered(LocalDateTime start, LocalDateTime end);

    // isto možeš napraviti za nedelju i mesec (ili koristiš day parametre da računaš start/end)

    // Update earnedXp + flag + status
    @Query("UPDATE task_instances SET earnedXp = :xp, isWithinQuota = :withinQuota WHERE id = :id")
    void updateEarnedXpAndQuota(int id, int xp, boolean withinQuota);

    // Update samo flag
    @Query("UPDATE task_instances SET isWithinQuota = :withinQuota WHERE id = :id")
    void updateWithinQuota(int id, boolean withinQuota);

    @Query("SELECT * FROM task_instances " +
            "WHERE isWithinQuota = 1 AND startExecutionTime BETWEEN :start AND :end")
    List<TaskInstance> getInQuotaTasksBetween(LocalDateTime start, LocalDateTime end);



}
