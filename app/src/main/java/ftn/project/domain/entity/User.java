package ftn.project.domain.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userId;

    private String username;
    private String password;
    private String email;
    private Integer powerPoints;
    private Integer experiencePoints;
    private Long coins;
    @NonNull
    private boolean emailVerified = false;
    private String avatarImage;

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPowerPoints(Integer powerPoints) {
        this.powerPoints = powerPoints;
    }

    public void setExperiencePoints(Integer experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public void setCoins(Long coins) {
        this.coins = coins;
    }

    public void setEmailVerified(boolean emailVerified){ this.emailVerified = emailVerified; }

    public void setEmail(String email) { this.email = email; }
    public void setAvatarImage(String avatarImage) { this.avatarImage = avatarImage; }
    public String getUsername(){
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public Integer getPowerPoints() {
        return powerPoints;
    }

    public Integer getExperiencePoints() {
        return experiencePoints;
    }

    public Long getCoins() {
        return coins;
    }

    public boolean getEmailVerified(){ return emailVerified; }
    public String getEmail() { return email; }
    public String getAvatarImage() { return avatarImage; }
}
