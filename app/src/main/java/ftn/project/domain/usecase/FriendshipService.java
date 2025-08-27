package ftn.project.domain.usecase;

import java.util.ArrayList;
import java.util.List;

import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.User;

public class FriendshipService {

    public ArrayList<UserFriendDTO> getFriendsForUser(ArrayList<Friendship> friendships, List<User> users, int userId){
        ArrayList<UserFriendDTO> friendDTOs = new ArrayList<>();
        for(Friendship friendship: friendships){
            int friendUid = friendship.getFirstUserId() != userId ? friendship.getFirstUserId() : friendship.getSecondUserId();
            User friend = users.stream()
                    .filter(u -> u.getUserId() == friendUid)
                    .findFirst()
                    .orElse(null);

            friendDTOs.add(new UserFriendDTO(friendUid, friend.getUsername()));
        }

        return friendDTOs;
    }
}
