package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.User;

@Dao
public interface AllianceRepositoryInterface {
    @Insert
    void insert(Alliance alliance);

    @Update
    void update(Alliance alliance);

    @Query("SELECT * FROM Alliance WHERE allianceId = :allianceID")
    Alliance getAlliance(int allianceID);
    @Query("SELECT * FROM Alliance WHERE leader_user_id = :userId")
    Alliance getAllianceByUser(int userId);

}
