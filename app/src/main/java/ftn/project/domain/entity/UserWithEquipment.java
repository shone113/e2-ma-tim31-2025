package ftn.project.domain.entity;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

public class UserWithEquipment {
    @Embedded
    public User user;
    @Relation(
            parentColumn = "userId",
            entityColumn = "equipmentId",
            associateBy = @Junction(UserEquipment.class)
    )
    public List<Equipment> equipmentList;
}
