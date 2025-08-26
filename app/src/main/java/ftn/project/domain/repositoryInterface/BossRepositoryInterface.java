package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import ftn.project.domain.entity.Boss;

@Dao
public interface BossRepositoryInterface {
    @Insert
    long insert(Boss boss);

    @Update
    void update(Boss boss);

    @Query("SELECT * FROM bosses WHERE id = :id")
    Boss getBossById(int id);

    @Query("SELECT * FROM bosses WHERE level = :level LIMIT 1")
    Boss getBossByLevel(int level);
    @Query("UPDATE bosses SET hp = 200 WHERE id= :id")
    void updateBoss(int id);
    @Query("UPDATE bosses SET isDefeated = 0 WHERE id= :id")
    void updateBossDef(int id);
}
