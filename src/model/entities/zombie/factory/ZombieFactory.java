package model.entities.zombie.factory;

import model.entities.zombie.Zombie;
import model.entities.zombie.loader.ZombieLoader;
import model.entities.zombie.loader.ArmorLoader;
import model.enums.ChapterZombieType;
import controller.menu.PreGameController;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieFactory {
    private static final List<Zombie> templates = ZombieLoader.loadZombies();
    private static final Random random = new Random();

    public static Zombie createZombie(String type) {
        return createZombie(type, 3);
    }

    public static Zombie createZombie(String type, int difficultyLevel) {
        if (type == null || type.trim().isEmpty()) {
            return applyDifficulty(new Zombie("NormalZombie", 200, 0.185, 20, 100), difficultyLevel);
        }

        String searchName = type.trim();

        for (Zombie template : templates) {
            if (template.getName().equalsIgnoreCase(searchName)) {
                double scaleIncrease = difficultyLevel / 3.0;
                double scaleDecrease = 3.0 / difficultyLevel;

                int hp = Math.max(1, (int) Math.round(template.getHealth() * scaleIncrease));
                int dmg = Math.max(1, (int) Math.round(template.getDamage() * scaleIncrease));
                double spd = template.getSpeed() * scaleIncrease;
                int cost = Math.max(10, (int) Math.round(template.getWaveCost() * scaleDecrease));

                Zombie zombie = new Zombie(
                        template.getName(),
                        hp,
                        spd,
                        dmg,
                        cost
                );

                if (template.getName().equalsIgnoreCase("ConeZombie")) {
                    int baseArmor = ArmorLoader.getBaseHealth("ConeDefault", 370);
                    zombie.setArmorHealth((int) Math.round(baseArmor * scaleIncrease));
                    zombie.setArmorType("CONE");
                } else if (template.getName().equalsIgnoreCase("BucketZombie")) {
                    int baseArmor = ArmorLoader.getBaseHealth("BucketDefault", 1100);
                    zombie.setArmorHealth((int) Math.round(baseArmor * scaleIncrease));
                    zombie.setArmorType("BUCKET");
                } else if (template.getName().equalsIgnoreCase("BrickZombie")) {
                    int baseArmor = ArmorLoader.getBaseHealth("BrickDefault", 2200);
                    zombie.setArmorHealth((int) Math.round(baseArmor * scaleIncrease));
                    zombie.setArmorType("BRICK");
                } else if (template.getName().equalsIgnoreCase("KnightZombie")) {
                    int baseArmor = ArmorLoader.getBaseHealth("CrownDefault", 1600);
                    zombie.setArmorHealth((int) Math.round(baseArmor * scaleIncrease));
                    zombie.setArmorType("KNIGHT");
                } else if (template.getName().equalsIgnoreCase("ZombieNewspaper")) {
                    int baseArmor = ArmorLoader.getBaseHealth("NewspaperDefault", 800);
                    zombie.setArmorHealth((int) Math.round(baseArmor * scaleIncrease));
                    zombie.setArmorType("NEWSPAPER");
                } else if (template.getName().equalsIgnoreCase("BarrelRollerZombie")) {
                    int barrelHealth = 500;
                    zombie.setBarrelHealth((int) Math.round(barrelHealth * scaleIncrease));
                    zombie.setBarrelRoller(true);
                    zombie.setBarrelDestroyed(false);
                } else if (template.getName().equalsIgnoreCase("ZombieIceAgeTroglobite")) {
                    int iceBlockHealth = 400;
                    zombie.setIceBlockHealth((int) Math.round(iceBlockHealth * scaleIncrease));
                    zombie.setTroglobite(true);
                    zombie.setIceBlockDestroyed(false);
                } else if (template.getName().equalsIgnoreCase("ZombieBeachSnorkel")) {
                    zombie.setUnderwater(true);
                    zombie.setHasSurfaced(false);
                } else if (template.getName().equalsIgnoreCase("ZombieDarkJuggler")) {
                    zombie.setInsanityThreshold(100);
                    zombie.setDamageTakenSinceLastReset(0);
                    zombie.setInsane(false);
                    zombie.setReflecting(false);
                } else if (template.getName().equalsIgnoreCase("ZombieWizard")) {
                    zombie.setWizard(true);
                    zombie.setTransformationCooldown(60);
                } else if (template.getName().equalsIgnoreCase("ZombieDarkKing")) {
                    zombie.setKing(true);
                    zombie.setIdle(true);
                    zombie.setSpawnX(8.0);
                } else if (template.getName().equalsIgnoreCase("ZombieIceAgeDodo")) {
                    zombie.setDodoRider(true);
                    zombie.setJumpCooldown(0);
                } else if (template.getName().equalsIgnoreCase("ZombieCrystalSkull")) {
                    zombie.setSunStealCooldown(30);
                    zombie.setSunStealTimer(0);
                    zombie.setStolenSunCount(0);
                }

                if (template.getName().equalsIgnoreCase("ZombieDarkImpDragon")) {
                    zombie.setImmuneToFreeze(true);
                }

                if (template.getName().equalsIgnoreCase("ZombieModernAllStar")) {
                    zombie.setCharging(true);
                }

                return zombie;
            }
        }

        for (Zombie template : templates) {
            if (template.getName().toLowerCase().contains(searchName.toLowerCase()) ||
                    searchName.toLowerCase().contains(template.getName().toLowerCase())) {
                return createZombie(template.getName(), difficultyLevel);
            }
        }

        return applyDifficulty(new Zombie(searchName, 200, 0.185, 20, 100), difficultyLevel);
    }

    private static Zombie applyDifficulty(Zombie z, int dl) {
        double scaleIncrease = dl / 3.0;
        double scaleDecrease = 3.0 / dl;
        return new Zombie(
                z.getName(),
                Math.max(1, (int) Math.round(z.getHealth() * scaleIncrease)),
                z.getSpeed() * scaleIncrease,
                Math.max(1, (int) Math.round(z.getDamage() * scaleIncrease)),
                Math.max(10, (int) Math.round(z.getWaveCost() * scaleDecrease))
        );
    }

    public static int getWaveCost(String type) {
        return getWaveCost(type, 3);
    }

    public static int getWaveCost(String type, int difficultyLevel) {
        Zombie z = createZombie(type, difficultyLevel);
        return z != null ? z.getWaveCost() : 100;
    }

    public static List<Zombie> generateWaveZombies(int waveDifficulty, int maxRows) {
        return generateWaveZombies(waveDifficulty, maxRows, 3);
    }

    public static List<Zombie> generateWaveZombies(int waveDifficulty, int maxRows, int difficultyLevel) {
        List<Zombie> waveList = new ArrayList<>();
        String currentChapter = PreGameController.activeChapterName;
        List<String> allowedZombieIds = ChapterZombieType.getAvailableZombiesForChapter(currentChapter);
        int currentCostAccumulated = 0;

        while (currentCostAccumulated < waveDifficulty) {
            String randomJsonId = allowedZombieIds.get(random.nextInt(allowedZombieIds.size()));
            int randomRow = random.nextInt(maxRows);
            int startColumn = 8;

            Zombie zombie = createZombie(randomJsonId, difficultyLevel);
            int cost = zombie.getWaveCost();

            if (currentCostAccumulated + cost > waveDifficulty + 50 && currentCostAccumulated > 0) {
                break;
            }

            zombie.setY(randomRow);
            zombie.setX(startColumn);
            waveList.add(zombie);
            currentCostAccumulated += cost;
        }
        return waveList;
    }

    public static Zombie createZombieAtColumn(String type, int lane, int column) {
        return createZombieAtColumn(type, lane, column, 3);
    }

    public static Zombie createZombieAtColumn(String type, int lane, int column, int difficultyLevel) {
        Zombie zombie = createZombie(type, difficultyLevel);
        if (zombie != null) {
            zombie.setX(column);
            zombie.setY(lane);
        }
        return zombie;
    }
}
