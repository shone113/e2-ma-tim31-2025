package ftn.project.domain.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    private int userId;

    private String username;
    private String password;
    private String email;
    @ColumnInfo(defaultValue = "0")
    private Integer powerPoints = 0;
    @ColumnInfo(defaultValue = "0")
    private Integer experiencePoints = 0;
    @ColumnInfo(defaultValue = "1000")
    private Long coins = 1000L;
    @NonNull
    @ColumnInfo(defaultValue = "0")
    private boolean emailVerified = false;
    private String avatarImage;
    @ColumnInfo(defaultValue = "1")
    private int level = 1;

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
    public void setLevel(int level) { this.level = level; }
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
    public int getLevel() { return level; }
}
