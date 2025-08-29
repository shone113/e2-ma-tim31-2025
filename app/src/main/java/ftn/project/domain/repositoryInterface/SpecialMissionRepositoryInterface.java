package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.SpecialMission;
import ftn.project.domain.entity.SpecialMissionProgress;

@Dao
public interface SpecialMissionRepositoryInterface {
    @Insert
    long insert(SpecialMission specialMission);
    @Update
    void update(SpecialMission specialMission);

    @Query("SELECT * FROM special_missions")
    List<SpecialMission> getAll();

    @Query("SELECT * FROM special_missions WHERE allianceId = :allianceID AND isActive = 1")
    List<SpecialMission> activeSpecialMissionByAlliance(int allianceID);

    @Query("SELECT * FROM special_missions WHERE id = :id")
    SpecialMission getMissionById(int id);

    @Query("SELECT sm.* " +
            "FROM special_missions sm " +
            "INNER JOIN special_mission_progress smp ON sm.id = smp.missionId " +
            "WHERE smp.userId = :userId AND sm.isActive = 1 " +
            "LIMIT 1")
    SpecialMission getActiveMissionForUser(int userId);

    @Query("SELECT * FROM special_mission_progress WHERE missionId = :missionId AND userId = :userId LIMIT 1")
    SpecialMissionProgress getProgressByMissionAndUser(int missionId, int userId);
}
