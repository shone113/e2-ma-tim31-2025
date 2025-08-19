package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    public int userId;

    public String username;
    public String password;

    public Integer powerPoints;
    public Integer experiencePoints;
    public Long coins;

    public String getUsername(){
        return username;
    }
}
