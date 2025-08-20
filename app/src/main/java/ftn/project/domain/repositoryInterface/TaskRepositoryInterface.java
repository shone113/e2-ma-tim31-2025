package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.Task;

@Dao
public interface TaskRepositoryInterface {
    @Insert
    long insert(Task task);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();

    @Update
    void update(Task task);
/*
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    Task getTaskById(int id);*/
}
