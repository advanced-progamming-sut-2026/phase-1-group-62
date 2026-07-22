package model.minigame;

import model.Game;
import model.Tile;
import model.entities.zombie.Zombie;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.zombie.factory.ZombieFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IZombie extends MiniGame {
    private int zombieSunCount;
    private int brainsEaten;
    private boolean[] brainRowEaten;
    private int stageLevel;
    private int maxStageLevel;
    private List<String> stageZombiePool;

    public IZombie() {
        super("IZombie");
        this.zombieSunCount = 150;
        this.brainsEaten = 0;
        this.brainRowEaten = new boolean[5];
        this.stageLevel = 1;
        this.maxStageLevel = 3;
        this.stageZombiePool = new ArrayList<>();
    }

    public void setupStage(Game game, int level) {
        this.stageLevel = level;
        this.brainsEaten = 0;
        this.brainRowEaten = new boolean[5];
        this.zombieSunCount = 150 + (level - 1) * 50;


        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            Tile t = game.getBoard().getTile(p.getY(), p.getX());
            if (t != null) t.setPlant(null);
            game.removePlant(p);
        }
        for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
            Tile t = game.getBoard().getTile(z.getY(), (int) z.getX());
            if (t != null) t.setZombie(null);
            game.removeZombie(z);
        }

        setupStageZombiePool(level);


        for (int row = 0; row < 5; row++) {
            Zombie sunZombie = new Zombie("SunProducerZombie", 200, 0.0, 0, 0);
            sunZombie.setArmorHealth(1100);
            sunZombie.setArmorType("BUCKET");
            sunZombie.setX(8.0);
            sunZombie.setY(row);
            game.addZombie(sunZombie);
            game.getBoard().getTile(row, 8).setZombie(sunZombie);
        }

        Random rand = new Random();
        String[] plantTypes;
        int minPlants, maxPlants;

        if (level == 1) {
            plantTypes = new String[]{"PeaShooter", "Sunflower", "WallNut"};
            minPlants = 1;
            maxPlants = 2;
        } else if (level == 2) {
            plantTypes = new String[]{"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Repeater"};
            minPlants = 2;
            maxPlants = 3;
        } else {
            plantTypes = new String[]{"Repeater", "SnowPea", "WallNut", "TallNut", "Cabbagepult", "Melonpult"};
            minPlants = 3;
            maxPlants = 4;
        }

        for (int row = 0; row < 5; row++) {
            int numPlants = minPlants + rand.nextInt(maxPlants - minPlants + 1);
            for (int i = 0; i < numPlants; i++) {
                int col = rand.nextInt(4);
                if (game.getBoard().getTile(row, col).getPlant() == null) {
                    String type = plantTypes[rand.nextInt(plantTypes.length)];
                    Plant p = PlantFactory.createPlant(type);
                    if (p == null) {
                        int hp = type.equalsIgnoreCase("WallNut") ? 400 : (type.equalsIgnoreCase("TallNut") ? 800 : 100);
                        int damage = type.equalsIgnoreCase("Repeater") ? 40 : 20;
                        p = new Plant(rand.nextInt(1000) + 200, type, "SHOOTER", null, 50, hp, damage, 2.0, 0, null, 0, null, 0);
                        p.initHealth();
                    }
                    p.setX(col);
                    p.setY(row);
                    game.addPlant(p);
                    game.getBoard().getTile(row, col).setPlant(p);
                }
            }
        }
    }

    private void setupStageZombiePool(int level) {
        stageZombiePool = new ArrayList<>();
        String[][] stagePools = {
                {"NormalZombie", "ConeZombie", "BucketZombie", "NewspaperZombie", "FootballZombie"},
                {"NormalZombie", "ConeZombie", "ProspectorZombie", "TurquoiseZombie", "AllStarZombie"},
                {"BucketZombie", "FootballZombie", "PianistZombie", "BarrelRollerZombie", "AllStarZombie"}
        };

        String[] pool = stagePools[Math.min(level - 1, stagePools.length - 1)];
        for (String type : pool) {
            stageZombiePool.add(type);
        }
    }

    public boolean placeZombie(String type, int lane, Game game) {
        return placeZombie(type, 8, lane, game);
    }

    public boolean placeZombie(String type, int col, int lane, Game game) {
        if (lane < 0 || lane >= 5) return false;
        if (col <= 4 || col >= 9) {
            game.getGameLogMessages().add("IZombie: Cannot place zombie past or on the plant boundary (Red Line at column 4)!");
            return false;
        }

        if (type.equalsIgnoreCase("SunProducerZombie")) {
            game.getGameLogMessages().add("IZombie: Error - SunProducerZombie cannot be deployed by player!");
            return false;
        }

        boolean valid = false;
        for (String z : stageZombiePool) {
            if (z.equalsIgnoreCase(type)) {
                valid = true;
                type = z;
                break;
            }
        }
        if (!valid) return false;

        int cost = getZombieCost(type);
        if (zombieSunCount < cost) return false;

        spendSun(cost);

        Zombie z = ZombieFactory.createZombieAtColumn(type, lane, col);
        if (z == null) {
            z = new Zombie(type, 200, 0.5, 20);
            z.setX(col);
            z.setY(lane);
        } else {
            z.setX((double) col);
            z.setY(lane);
        }
        game.addZombie(z);
        game.getBoard().getTile(lane, col).setZombie(z);
        game.getGameLogMessages().add("IZombie: Placed " + type + " at (" + col + ", " + lane + ") for " + cost + " suns.");
        return true;
    }

    public int getZombieCost(String type) {
        switch (type.toLowerCase()) {
            case "normalzombie": return 50;
            case "conezombie": return 75;
            case "newspaperzombie": return 100;
            case "prospectorzombie": return 110;
            case "turquoisezombie": return 120;
            case "bucketzombie": return 125;
            case "pianistzombie": return 130;
            case "barrelrollerzombie": return 140;
            case "footballzombie": return 150;
            case "allstarzombie": return 160;
            default: return 50;
        }
    }

    public int getMinZombieCostInPool() {
        int min = Integer.MAX_VALUE;
        for (String z : stageZombiePool) {
            min = Math.min(min, getZombieCost(z));
        }
        return min == Integer.MAX_VALUE ? 50 : min;
    }

    public int getZombieSunCount() { return zombieSunCount; }
    public void setZombieSunCount(int zombieSunCount) { this.zombieSunCount = zombieSunCount; }
    public void spendSun(int amount) { this.zombieSunCount -= amount; }
    public void addSun(int amount) { this.zombieSunCount += amount; }
    public int getBrainsEaten() { return brainsEaten; }

    public boolean isBrainRowEaten(int row) {
        if (row >= 0 && row < 5) return brainRowEaten[row];
        return true;
    }

    public void eatBrain(int row) {
        if (row >= 0 && row < 5 && !brainRowEaten[row]) {
            brainRowEaten[row] = true;
            brainsEaten++;
        }
    }

    public boolean isVictoryConditionMet() { return brainsEaten >= 5; }
    public int getStageLevel() { return stageLevel; }
    public List<String> getStageZombiePool() { return new ArrayList<>(stageZombiePool); }

    public void updateMiniGame(Game game) {
        if (game.getTickCount() == 1) {
            setupStage(game, stageLevel);
        }


        for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
            if (z.getName().equalsIgnoreCase("SunProducerZombie")) {
                z.incrementIzombieSunTicks();
                int interval = Math.max(150 - (z.getIzombieSunProductionTicks() / 4), 30);
                if (game.getTickCount() % interval == 0) {
                    addSun(25);
                    game.getGameLogMessages().add("IZombie: SunProducerZombie in lane " + z.getY() + " generated 25 suns.");
                }
            }
        }

        if (isVictoryConditionMet()) {
            completeLevel(stageLevel, brainsEaten);
            if (stageLevel < maxStageLevel) {
                stageLevel++;
                game.getGameLogMessages().add("IZombie: Stage " + (stageLevel - 1) + " complete! Advancing to Stage " + stageLevel);
                setupStage(game, stageLevel);
                return;
            } else {
                game.setWon(true);
                game.stop();
                game.getGameLogMessages().add("IZombie: All 3 Stages Completed Successfully! Victory!");
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                return;
            }
        }

        boolean hasAttackingZombie = false;
        for (Zombie z : game.getActiveZombies()) {
            if (!z.getName().equalsIgnoreCase("SunProducerZombie")) {
                hasAttackingZombie = true;
                break;
            }
        }

        if (zombieSunCount < getMinZombieCostInPool() && !hasAttackingZombie) {
            game.setLost(true);
            game.stop();
            game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
        }
    }
}