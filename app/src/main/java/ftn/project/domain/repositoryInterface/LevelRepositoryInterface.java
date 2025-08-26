package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import ftn.project.domain.entity.Level;

@Dao
public interface LevelRepositoryInterface {

    @Query("SELECT * FROM Level")
    List<Level> getAll();

}
