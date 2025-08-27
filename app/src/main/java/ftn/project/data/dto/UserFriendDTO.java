package ftn.project.data.dto;

import androidx.room.Ignore;

public class UserFriendDTO {
    public int userId;
    public String username;
    public boolean friend;

    public UserFriendDTO(){}

    @Ignore
    public UserFriendDTO(int userId, String username){
        this.userId = userId;
        this.username = username;
        this.friend = false;
    }
    @Ignore
    public UserFriendDTO(int userId, String username, boolean friend){
        this.userId = userId;
        this.username = username;
        this.friend = friend;
    }
}
