package ftn.project.domain.usecase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import ftn.project.data.dto.UserFriendDTO;
import ftn.project.domain.entity.AllianceInvitation;
import ftn.project.domain.entity.Friendship;
import ftn.project.domain.entity.InvitationStatus;
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

            friendDTOs.add(new UserFriendDTO(friendUid, friend.getUsername(), true));
        }
        return friendDTOs;
    }

    public ArrayList<UserFriendDTO> getFriendsWithInvitationForUser(ArrayList<Friendship> friendships, List<User> users, int userId, List<AllianceInvitation> allianceInvitations){
        ArrayList<UserFriendDTO> friendDTOs = new ArrayList<>();

        for (Friendship friendship : friendships) {
            int friendUid = (friendship.getFirstUserId() != userId)
                    ? friendship.getFirstUserId()
                    : friendship.getSecondUserId();

            // nađi User objekat (čuvaj se NPE)
            User friend = users.stream()
                    .filter(u -> u.getUserId() == friendUid)
                    .findFirst()
                    .orElse(null);
            if (friend == null) continue;

            // nađi pozivnicu između userId i friendUid (u bilo kom smeru)
            AllianceInvitation match = null;
            for (AllianceInvitation ai : allianceInvitations) {
                boolean between =
                        (ai.getInviterUserId() == userId && ai.getInviteeUserId() == friendUid) ||
                                (ai.getInviterUserId() == friendUid && ai.getInviteeUserId() == userId);
                if (!between) continue;

                match = ai;
            }

            UserFriendDTO dto = new UserFriendDTO(friendUid, friend.getUsername(), true);
            if (match != null) {
                dto.invitationStatus = match.status;
            }
            friendDTOs.add(dto);
        }
        return friendDTOs;
    }

}
