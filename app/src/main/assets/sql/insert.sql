INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(1, 'potion_1', 'POTION', 'STRENGTH', 'ONE_USE', 20.0, 50.0, 'potion_1');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(2, 'potion_2', 'POTION', 'STRENGTH', 'ONE_USE', 40.0, 70.0, 'potion_2');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(3, 'potion_3', 'POTION', 'STRENGTH', 'PERMANENT', 5.0, 200.0, 'potion_3');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(4, 'potion_4', 'POTION', 'STRENGTH', 'PERMANENT', 10.0, 1000.0, 'potion_4');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(5, 'gloves', 'ARMOR', 'STRENGTH', 'TWO_USES', 10.0, 60.0, 'gloves');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(6, 'shield', 'ARMOR', 'ATTACK_CHANCE', 'TWO_USES', 10.0, 60.0, 'shield');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(7, 'boots', 'ARMOR', 'EXTRA_ATTACK', 'TWO_USES', 40.0, 80.0, 'boots');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(8, 'sword', 'WEAPON', 'STRENGTH', 'PERMANENT', 5.0, NULL, 'sword');

INSERT INTO Equipment
(equipmentId, name, type, effectType, activeType, bonusPercentage, costPercentageOfReward, imageName)
VALUES
(9, 'bow_and_arrow', 'WEAPON', 'COINS', 'PERMANENT', 5.0, NULL, 'bow_and_arrow');


INSERT INTO Level (levelNumber, requiredXP, titleImage, titleNameKey, titleIconKey)
VALUES (1, 200, 'title_1', 'owl_egg', 'title_1');

INSERT INTO Level (levelNumber, requiredXP, titleImage, titleNameKey, titleIconKey)
VALUES (2, 500, 'title_2', 'owl_baby', 'title_2');

INSERT INTO Level (levelNumber, requiredXP, titleImage, titleNameKey, titleIconKey)
VALUES (3, 1250, 'title_3', 'owl_school', 'title_3');

INSERT INTO Level (levelNumber, requiredXP, titleImage, titleNameKey, titleIconKey)
VALUES (4, 3125, 'title_4', 'owl_aviator', 'title_4');

INSERT INTO Level (levelNumber, requiredXP, titleImage, titleNameKey, titleIconKey)
VALUES (5, 7813, 'title_5', 'owl_pedia', 'title_5');
