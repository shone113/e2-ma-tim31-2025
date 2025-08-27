package ftn.project.domain.repositoryInterface;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

import ftn.project.data.dto.UserFriendDTO;
import ftn.project.data.dto.UserStatsDTO;
import ftn.project.domain.entity.Task;
import ftn.project.domain.entity.User;


@Dao
public interface UserRepositoryInterface {
    @Insert
    void insert(User user);

    @Query("SELECT * FROM User")
    List<User> getAll();

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

    @Query("UPDATE User SET coins = :coins AND level = :level " +
            "WHERE userId = :userId")
    int testUserUpdate(int userId, long coins, int level);

    interface OnUsersFound { void onResult(java.util.List<UserFriendDTO> results); }

    @Query("SELECT userId, username, 0 AS friend FROM User WHERE username LIKE '%' || :username || '%' LIMIT 50")
    List<UserFriendDTO> searchUsersByUsername(String username);

}
