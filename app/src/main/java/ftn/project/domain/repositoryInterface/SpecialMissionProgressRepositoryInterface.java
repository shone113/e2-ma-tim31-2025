package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.Battle;
import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;
import ftn.project.domain.entity.SpecialMissionWithProgress;

@Dao
public interface SpecialMissionProgressRepositoryInterface {
    @Insert
    void insert(SpecialMissionProgress specialMissionProgress);

    @Query("SELECT * FROM special_mission_progress")
    List<SpecialMissionProgress> getAll();

    @Query("SELECT * FROM special_mission_progress WHERE missionId = :missionId")
    List<SpecialMissionProgress> getAllByMission(int missionId);
    @Update
    void update(SpecialMissionProgress specialMissionProgress);

    @Query("SELECT * FROM special_mission_progress WHERE missionId = :missionId AND userId = :userId")
    SpecialMissionProgress getProgressByMissionAndUser(int missionId, int userId);

    @Query("SELECT * FROM special_mission_progress WHERE missionId = :missionId")
    List<SpecialMissionProgress> getAllProgressForMission(int missionId);

    @Query("SELECT * FROM special_mission_progress WHERE userId = :userId")
    List<SpecialMissionProgress> getAllProgressForUser(int userId);

    @Query("SELECT EXISTS(SELECT 1 FROM special_mission_progress WHERE userId = :userId LIMIT 1)")
    boolean existsByUserId(int userId);
}
