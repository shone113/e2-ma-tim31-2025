package ftn.project.domain.usecase;

import java.util.ArrayList;
import java.util.List;

import ftn.project.data.db.AppDatabase;
import ftn.project.data.dto.UserEquipmentDTO;
import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.User;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.domain.repositoryInterface.BossRepositoryInterface;
import ftn.project.domain.repositoryInterface.EquipmentRepositoryInterface;
import ftn.project.domain.repositoryInterface.UserEquipmentRepositoryInterface;

public class EquipmentActivationService {

    private final UserEquipmentRepositoryInterface userEquipmentRepository;
    private final EquipmentRepositoryInterface equipmentRepository;

    public EquipmentActivationService(AppDatabase db){
        this.userEquipmentRepository = db.userEquipmentRepository();
        this.equipmentRepository = db.equipmentRepository();
    }

    public ArrayList<UserEquipmentDTO> getEquipmentForUser(int userId){
        ArrayList<UserEquipmentDTO> userEquipmentDTOs = new ArrayList<>();

        Equipment equipment;
        UserEquipmentDTO userEquipmentDTO;
        List<UserEquipment> userEquipment = userEquipmentRepository.getAllForUser(userId);
        for(UserEquipment ue : userEquipment){
            equipment = equipmentRepository.getById(ue.getEquipmentId());

            userEquipmentDTO = new UserEquipmentDTO();
            userEquipmentDTO.userEquipmentId = ue.getUserEquipmentId();
            userEquipmentDTO.effectType = equipment.getEffectType();
            userEquipmentDTO.activeType = equipment.getActiveType();
            userEquipmentDTO.bonusPercentage = equipment.getBonusPercentage();
            userEquipmentDTO.imageName = equipment.getImageName();
            userEquipmentDTO.userId = userId;
            userEquipmentDTO.active = ue.getActive();
            userEquipmentDTO.battleCount = ue.getBattleCount();

            userEquipmentDTOs.add(userEquipmentDTO);
        }
        return userEquipmentDTOs;
    }
}
