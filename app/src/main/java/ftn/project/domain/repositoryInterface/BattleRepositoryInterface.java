package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.Battle;

@Dao
public interface BattleRepositoryInterface {
    @Insert
    long insert(Battle battle);

    @Update
    void update(Battle battle);

    @Query("SELECT * FROM battles WHERE id = :id LIMIT 1")
    Battle getBattleById(int id);

    @Query("SELECT * FROM battles")
    List<Battle> getAllBattles();

}
