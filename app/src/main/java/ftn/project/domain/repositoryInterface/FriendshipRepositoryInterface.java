package ftn.project.domain.repositoryInterface;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import java.util.List;

import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.User;

@Dao
public interface FriendshipRepositoryInterface {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Friendship f);
    @Upsert
    void upsert(Friendship f);

    @Query("SELECT * FROM Friendship WHERE firstUserId = :id OR secondUserId = :id")
    List<Friendship> getAllForUserId(int id);

    @Query("SELECT * FROM Friendship WHERE (firstUserId = :firstUserId AND secondUserId = :secondUserId)" +
            " OR  (firstUserId = :secondUserId AND secondUserId = :firstUserId) LIMIT 1")
    Friendship getFriendhipForUsers(int firstUserId, int secondUserId);

    @Query("DELETE FROM Friendship WHERE firstUserId = :userId OR secondUserId = :userId")
    void deleteAllForUser(int userId);

    @Query(" SELECT EXISTS( " +
           "SELECT 1 FROM Friendship " +
           "WHERE (firstUserId = :a AND secondUserId = :b) " +
            "OR (firstUserId = :b AND secondUserId = :a))")
    boolean existsBetween(int a, int b);

//    @Query("SELECT u.* FROM User uJOIN Friendship f ON u.userId = CASE WHEN f.requesterId = :me "
//    + "THEN f.addresseeId ELSE f.requesterId END "
//    + "WHERE (f.requesterId = :me OR f.addresseeId = :me) AND f.status = 'ACCEPTED'")
//    List<User> getAcceptedFriends(int me);
}
