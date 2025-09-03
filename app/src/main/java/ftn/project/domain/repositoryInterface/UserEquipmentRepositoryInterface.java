package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Junction;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Relation;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;

@Dao
public interface UserEquipmentRepositoryInterface {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    void add(UserEquipment link);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addAll(List<UserEquipment> links);

    @Update
    int update(UserEquipment userEquipment);

    @Query("SELECT * FROM UserEquipment")
    List<UserEquipment> getAll();

    @Query("SELECT * FROM UserEquipment WHERE userId = :uid")
    List<UserEquipment> getAllForUser(int uid);

    @Query("SELECT * FROM UserEquipment WHERE userId=:userId AND equipmentId=:equipmentId LIMIT 1")
    UserEquipment getByUserAndEquipment(int userId, int equipmentId);

    @Query("SELECT * FROM UserEquipment WHERE userEquipmentId=:userEquipmentId LIMIT 1")
    UserEquipment getById(int userEquipmentId);

    @Query("DELETE FROM UserEquipment WHERE userId = :uid AND equipmentId = :eid")
    void remove(int uid, int eid);

    @Query("DELETE FROM UserEquipment WHERE userId = :uid")
    void removeAllForUser(int uid);

    @Query("UPDATE UserEquipment SET active = 1 WHERE userEquipmentId = :userEquipmentId")
    void activateEquipment(int userEquipmentId);
}
