package ftn.project.data.dto;

import androidx.annotation.Nullable;

import ftn.project.domain.entity.ActiveType;
import ftn.project.domain.entity.EffectType;
import ftn.project.domain.entity.EquipmentType;

public class UserEquipmentDTO {

    public int userEquipmentId;
    public String name;
    public EquipmentType type;
    public EffectType effectType;
    public ActiveType activeType;
    public Double bonusPercentage;
    public String imageName;
    public int userId;
    public Integer battleCount;
    public boolean active;
    public UserEquipmentDTO() {}

}
