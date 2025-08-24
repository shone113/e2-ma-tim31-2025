package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Junction;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Relation;

import java.util.List;

import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;

@Dao
public interface UserEquipmentRepositoryInterface {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void add(UserEquipment link);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addAll(List<UserEquipment> links);

    @Query("SELECT * FROM UserEquipment")
    List<UserEquipment> getAll();
    // Unlink
    @Query("DELETE FROM UserEquipment WHERE userId = :uid AND equipmentId = :eid")
    void remove(int uid, int eid);

    @Query("DELETE FROM UserEquipment WHERE userId = :uid")
    void removeAllForUser(int uid);
}
