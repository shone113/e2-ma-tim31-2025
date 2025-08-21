package ftn.project.domain.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Equipment {
    @PrimaryKey(autoGenerate = true)
    private int equipmentId;
    private String name;
    private EquipmentType type;
    private EffectType effectType;
    private ActivationType activationType;
    private double bonusPercentage;
    private double costPercentageOfReward;
    private Integer battleCount;
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

    public ActivationType getActivationType() {
        return activationType;
    }

    public void setActivationType(ActivationType activationType) {
        this.activationType = activationType;
    }

    public double getBonusPercentage() {
        return bonusPercentage;
    }

    public void setBonusPercentage(double bonusPercentage) {
        this.bonusPercentage = bonusPercentage;
    }

    public double getCostPercentageOfReward(){
        return costPercentageOfReward;
    }

    public void setCostPercentageOfReward(double costPercentageOfReward){
        this.costPercentageOfReward = costPercentageOfReward;
    }

    public Integer getBattleCount(){
        return battleCount;
    }

    public void setBattleCount(Integer battleCount){
        this.battleCount = battleCount;
    }

    public String getImageName(){
        return imageName;
    }

    public void setImageName(String imageName){
        this.imageName = imageName;
    }
}
