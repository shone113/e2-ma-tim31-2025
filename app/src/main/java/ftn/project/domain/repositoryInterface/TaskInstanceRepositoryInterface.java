package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.TaskInstance;

@Dao
public interface TaskInstanceRepositoryInterface {
    @Insert
    void insert(TaskInstance taskInstance);
    @Query("SELECT * FROM task_instances")
    List<TaskInstance> getAllTasksInstances();
}
