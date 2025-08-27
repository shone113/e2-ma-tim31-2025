package ftn.project.domain.usecase;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

import ftn.project.data.dto.LevelDTO;
import ftn.project.domain.entity.Level;
import ftn.project.domain.entity.User;

public class LevelAdvancementService {

    public LevelAdvancementService() {}

    public ArrayList<LevelDTO> getLevelsForUser(ArrayList<Level> levels, User user){
        ArrayList<LevelDTO> levelDTOs = new ArrayList<>();
        for(Level level: levels){
            int remainingXP;
            if(user.getExperiencePoints() < level.getRequiredXP()){
                remainingXP = level.getRequiredXP() - user.getExperiencePoints();
            } else {
                remainingXP = 0;
            }
            boolean reachedStatus = user.getLevel() <= level.getLevelNumber() ? true : false;
            levelDTOs.add(new LevelDTO(level.getLevelNumber(), level.getTitleIconKey(), level.getRequiredXP(), remainingXP, reachedStatus));
        }
        return levelDTOs;
    }
}
