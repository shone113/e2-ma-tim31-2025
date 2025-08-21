package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Query;

import java.util.List;

import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;

@Dao
public interface EquipmentRepositoryInterface {

    @Query("SELECT * FROM Equipment")
    List<Equipment> getAll();

}
