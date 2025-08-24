package ftn.project.domain.entity;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Equipment {
    @PrimaryKey(autoGenerate = true)
    private int equipmentId;
    private String name;
    private EquipmentType type;
    private EffectType effectType;
    private ActiveType activeType;
    private Double bonusPercentage;
    @Nullable
    private Double costPercentageOfReward;
    private String imageName;

    public Equipment(){}

    public int getEquipmentId(){
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId){
        this.equipmentId = equipmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EquipmentType getType() {
        return type;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public EffectType getEffectType() {
        return effectType;
    }

    public void setEffectType(EffectType effectType) {
        this.effectType = effectType;
    }

    public ActiveType getActiveType() {
        return activeType;
    }

    public void setActiveType(ActiveType activeType) {
        this.activeType = activeType;
    }

    public double getBonusPercentage() {
        return bonusPercentage;
    }

    public void setBonusPercentage(double bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
    }

    public Double getCostPercentageOfReward(){
        return costPercentageOfReward;
    }

    public void setCostPercentageOfReward(double costPercentageOfReward){
        this.costPercentageOfReward = costPercentageOfReward;
    }

    public String getImageName(){
        return imageName;
    }

    public void setImageName(String imageName){
        this.imageName = imageName;
    }
}
