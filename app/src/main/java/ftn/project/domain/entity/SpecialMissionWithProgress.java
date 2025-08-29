package ftn.project.domain.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class SpecialMissionWithProgress {
    @Embedded
    public SpecialMission mission;

    @Relation(
            parentColumn = "id",
            entityColumn = "missionId"
    )
    public List<SpecialMissionProgress> progressList;
}