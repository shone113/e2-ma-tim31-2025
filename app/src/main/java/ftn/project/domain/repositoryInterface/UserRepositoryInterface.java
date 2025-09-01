package ftn.project.domain.repositoryInterface;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.project.data.dto.UserFriendDTO;
import ftn.project.data.dto.UserStatsDTO;
import ftn.project.domain.entity.User;


@Dao
public interface UserRepositoryInterface {
    @Insert
    long insert(User user);

    @Update
    int update(User user);

    @Query("SELECT * FROM User")
    List<User> getAll();

    //@Update
    //void update(User user);

    @Query("SELECT * FROM User WHERE firebaseUid = :uid LIMIT 1")
    LiveData<User> observeByFirebaseUid(String uid);

    @Query("SELECT * FROM User WHERE userId = :id LIMIT 1")
    User getById(int id);

    @Query("SELECT * FROM User WHERE firebaseUid = :firebase LIMIT 1")
    User getByFirebaseUid(String firebase);
    @Query("SELECT * FROM User WHERE firebaseUid != :firebaseUid")
    List<User> getAllUsersExceptLogged(String firebaseUid);
    @Query("SELECT coins, powerPoints, level, experiencePoints FROM User WHERE firebaseUid = :uid LIMIT 1")
    LiveData<UserStatsDTO> observeStats(String uid);
    @Query("UPDATE User SET emailVerified = :verified WHERE userId = :uid")
    void markVerified(String uid, boolean verified);

    @Query("UPDATE User SET coins = coins - :coins WHERE userId = :userId")
    void subtractCoins(int userId, long coins);
    @Query("UPDATE User SET coins = coins + :coins WHERE userId = :userId")
    void addCoins(int userId, long coins);

    @Query("UPDATE User SET coins = :coins AND level = :level " +
            "WHERE userId = :userId")
    int testUserUpdate(int userId, long coins, int level);

    interface OnUsersFound { void onResult(java.util.List<UserFriendDTO> results); }

    @Query(
            "SELECT u.userId, u.username, 0 AS friend " +
                    "FROM User u " +
                    "WHERE u.userId != :currentUserId " +
                    "  AND u.username LIKE '%' || :q || '%' COLLATE NOCASE " +
                    "  AND NOT EXISTS ( " +
                    "        SELECT 1 FROM Friendship f " +
                    "        WHERE (f.firstUserId = :currentUserId AND f.secondUserId = u.userId) " +
                    "           OR (f.secondUserId = :currentUserId AND f.firstUserId = u.userId) " +
                    "  ) " +
                    "ORDER BY u.username " +
                    "LIMIT 50"
    )
    List<UserFriendDTO> searchNonFriendUsersWithFlag(String q, int currentUserId);

    @Query("SELECT userId, username, 0 AS friend FROM User WHERE username LIKE '%' || :username || '%' LIMIT 50")
    List<UserFriendDTO> searchUsersByUsername(String username);
    @Query("UPDATE User SET experiencePoints = :xP " +
            "WHERE userId = :userId")
    void updateExperiencePoints(int userId, int xP);
    @Query("UPDATE User SET level = :level " +
            "WHERE userId = :userId")
    void updateLevel(int userId, int level);

    @Query("SELECT alliance_id FROM User WHERE userId = :userId")
    int getLoggedUserAlliance(int userId);

    @Query("SELECT * FROM User WHERE alliance_id = :allianceId")
    List<User> getAllUserInAlliance(int allianceId);

    @Query("SELECT COUNT(userId) FROM USER WHERE alliance_id = :allianceId")
    int allianceCount(int allianceId);
}
