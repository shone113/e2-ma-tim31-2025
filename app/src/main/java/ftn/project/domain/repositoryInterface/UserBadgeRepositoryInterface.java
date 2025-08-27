package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

import ftn.project.domain.entity.UserBadge;

@Dao
public interface UserBadgeRepositoryInterface {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(UserBadge ub);

    @Query("SELECT * FROM userbadge")
    List<UserBadge> getAll();

    @Query("SELECT badgeCode FROM UserBadge WHERE userId = :userId")
    List<String> getBadgeCodesForUser(int userId);

    @Query("SELECT COUNT(*) FROM UserBadge WHERE userId = :userId AND badgeCode = :badgeCode")
    int hasBadge(int userId, String badgeCode);
}
