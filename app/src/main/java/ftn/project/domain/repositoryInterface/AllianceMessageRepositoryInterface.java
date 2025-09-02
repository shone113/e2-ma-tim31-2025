package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.domain.entity.Alliance;
import ftn.project.domain.entity.AllianceMessage;

@Dao
public interface AllianceMessageRepositoryInterface {
    @Insert
    void insert(AllianceMessage allianceMessage);

    @Query("SELECT * FROM AllianceMessage WHERE allianceId = :allianceId ORDER BY sentAt")
    List<AllianceMessage> getAllianceMessages(int allianceId);
}
