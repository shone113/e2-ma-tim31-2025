package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import ftn.project.domain.entity.User;

@Dao
public interface UserRepositoryInterface {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM User")
    List<User> getAll();
}
