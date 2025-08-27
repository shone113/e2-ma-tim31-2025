package ftn.project.domain.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(tableName = "UserBadge",
        primaryKeys = {"userId", "badgeCode"},
        indices = {@Index("userId"), @Index("badgeCode")})
public class UserBadge {
    public int userId;
    @NonNull
    public String badgeCode;
}
