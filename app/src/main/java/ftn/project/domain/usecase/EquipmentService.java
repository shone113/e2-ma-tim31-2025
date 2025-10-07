package ftn.project.domain.usecase;

import java.util.List;
import java.util.Random;

import ftn.project.domain.entity.Equipment;
import ftn.project.domain.entity.EquipmentType;
import ftn.project.domain.entity.UserEquipment;
import ftn.project.domain.repositoryInterface.EquipmentRepositoryInterface;
import ftn.project.domain.repositoryInterface.SpecialMissionRepositoryInterface;

public class EquipmentService {

    private final EquipmentRepositoryInterface equipmentRepository;
    private final Random random = new Random();

    public EquipmentService(EquipmentRepositoryInterface equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    /**
     * Vrati random equipment za korisnika (bez čuvanja u bazu).
     */
    public Equipment generateRandomClothes() {
        EquipmentType type = EquipmentType.ARMOR;

        List<Equipment> pool = equipmentRepository.getAllByType(type);

        if (pool == null || pool.isEmpty()) {
            return null; // nema dostupnog armora u bazi
        }

        return pool.get(random.nextInt(pool.size()));
    }

    public Equipment generateRandomPotion() {
        EquipmentType type = EquipmentType.POTION;

        List<Equipment> pool = equipmentRepository.getAllByType(type);

        if (pool == null || pool.isEmpty()) {
            return null;
        }

        return pool.get(random.nextInt(pool.size()));
    }

}
