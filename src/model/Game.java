package model;

import model.enums.Difficulty;
import model.entities.zombie.Spawner;
import model.entities.zombie.Zombie;
import model.entities.zombie.ZombieEffect;
import model.entities.plant.Plant;
import model.score.ScoreGame;
import model.greenhouse.Greenhouse;
import model.enums.TileType;
import model.enums.SpecialLevelType;
import model.minigame.MiniGame;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.minigame.IZombie;
import model.minigame.Zombotany;
import model.minigame.Beghoul;
import model.season.Season;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private Board board;
    private Level level;
    private Difficulty difficulty;
    private int difficultyLevel;
    private int sunCount;
    private boolean running;
    private int coins;
    private int diamonds;
    private int plantFoodCount;
    private Spawner spawner;
    private ScoreGame scoreGame;
    private Greenhouse greenhouse;
    private List<Bullet> bullets;
    private List<Sun> suns;
    private List<Zombie> activeZombies;
    private List<Plant> activePlants;
    private int tickCount;
    private LawnMower[] lawnMowers;
    private boolean won;
    private boolean lost;
    private int lastSunDropTick;
    private int lastPrintedWave;
    private Season currentSeason;
    private List<String> conveyorBeltPlants;
    private int lastConveyorSpawnTick;
    private List<Plant> seedsToProtect;
    private int zombiesKilledInLevel;
    private int sunsProducedInLevel;
    private int plantsLostCount;
    private boolean zombieWavesStarted;
    private MiniGame activeMiniGame;
    private List<String> gameLogMessages = new ArrayList<>();

    public Game() {
        this.board = new Board(5, 9);
        this.level = new Level(1);
        this.difficulty = Difficulty.NORMAL;
        this.difficultyLevel = 3;
        this.sunCount = 50;
        this.coins = 0;
        this.diamonds = 0;
        this.plantFoodCount = 0;
        this.bullets = new ArrayList<>();
        this.suns = new ArrayList<>();
        this.activeZombies = new ArrayList<>();
        this.activePlants = new ArrayList<>();
        this.scoreGame = new ScoreGame();
        this.tickCount = 0;
        this.lawnMowers = new LawnMower[5];
        for (int i = 0; i < 5; i++) {
            lawnMowers[i] = new LawnMower(i);
        }
        this.won = false;
        this.lost = false;
        this.lastSunDropTick = 0;
        this.lastPrintedWave = 0;
        this.currentSeason = new Season("Normal", 10);
        this.conveyorBeltPlants = new ArrayList<>();
        this.lastConveyorSpawnTick = 0;
        this.seedsToProtect = new ArrayList<>();
        this.zombiesKilledInLevel = 0;
        this.sunsProducedInLevel = 0;
        this.plantsLostCount = 0;
        this.zombieWavesStarted = true;
        this.activeMiniGame = null;
    }

    public Game(int rows, int columns, int levelNumber, int difficultyLevel) {
        this();
        this.board = new Board(rows, columns);
        this.level = new Level(levelNumber);
        this.difficultyLevel = difficultyLevel;
        if (difficultyLevel <= 2) {
            this.difficulty = Difficulty.EASY;
        } else if (difficultyLevel >= 4) {
            this.difficulty = Difficulty.HARD;
        } else {
            this.difficulty = Difficulty.NORMAL;
        }
        this.spawner = new Spawner(board, levelNumber * 2, this.difficulty);
        this.lawnMowers = new LawnMower[rows];
        for (int i = 0; i < rows; i++) {
            lawnMowers[i] = new LawnMower(i);
        }
    }

    public Game(int rows, int columns, int levelNumber, Difficulty difficulty) {
        this(rows, columns, levelNumber, difficulty == Difficulty.EASY ? 1 : (difficulty == Difficulty.HARD ? 5 : 3));
    }

    public void start() {
        running = true;
        if (spawner != null) {
            spawner.startWave(1);
            gameLogMessages.add("Wave " + spawner.getCurrentWave() + " started.");
        }
    }

    public void stop() {
        running = false;
    }

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(Season currentSeason) {
        this.currentSeason = currentSeason;
        if (this.spawner != null) {
            this.spawner.setCurrentSeason(currentSeason);
        }
        if (currentSeason != null) {
            currentSeason.setupEnvironment(this);
        }
    }

    public void setupSpecialLevelFeatures() {
        if (level == null) return;
        SpecialLevelType type = level.getSpecialLevelType();
        if (type == SpecialLevelType.SAVE_OUR_SEEDS) {
            for (int[] pos : level.getSeedProtectionPositions()) {
                Plant p = model.entities.plant.factory.PlantFactory.createPlant("PeaShooter");
                if (p != null) {
                    p.setX(pos[1]);
                    p.setY(pos[0]);
                    addPlant(p);
                    board.getTile(pos[0], pos[1]).setPlant(p);
                    seedsToProtect.add(p);
                }
            }
        } else if (type == SpecialLevelType.PLANT_WHAT_YOU_GET) {
            this.sunCount = level.getInitialSunAmount();
            this.zombieWavesStarted = false;
        }
    }

    private String getRandomUnlockedPlant() {
        List<String> unlocked = new ArrayList<>();
        if (model.UserSession.isLoggedIn() && model.UserSession.getCurrentUser() != null) {
            unlocked = model.UserSession.getCurrentUser().getUnlockedPlants();
        }
        if (unlocked == null || unlocked.isEmpty()) {
            unlocked = new ArrayList<>();
            unlocked.add("PeaShooter");
        }
        return unlocked.get(new Random().nextInt(unlocked.size()));
    }

    public void startZombieWaves() {
        this.zombieWavesStarted = true;
    }

    public boolean isZombieWavesStarted() {
        return zombieWavesStarted;
    }

    public void setZombieWavesStarted(boolean zombieWavesStarted) {
        this.zombieWavesStarted = zombieWavesStarted;
    }

    public List<String> getConveyorBeltPlants() {
        return conveyorBeltPlants;
    }

    public List<Plant> getSeedsToProtect() {
        return seedsToProtect;
    }

    public int getZombiesKilledInLevel() {
        return zombiesKilledInLevel;
    }

    public int getPlantsLostCount() {
        return plantsLostCount;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public void setWon(boolean won) {
        this.won = won;
    }

    public void setSunCount(int sunCount) {
        this.sunCount = sunCount;
    }

    public MiniGame getActiveMiniGame() {
        return activeMiniGame;
    }

    public void setActiveMiniGame(MiniGame activeMiniGame) {
        this.activeMiniGame = activeMiniGame;
    }

    public List<String> getGameLogMessages() {
        List<String> logCopy = new ArrayList<>(gameLogMessages);
        gameLogMessages.clear();
        return logCopy;
    }

    public void incrementZombiesKilled() {
        this.zombiesKilledInLevel++;
    }

    public void incrementPlantsLost() {
        this.plantsLostCount++;
    }

    public void tick() {
        if (!running || won || lost) return;
        tickCount++;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                board.getTile(r, c).setZombie(null);
            }
        }

        for (Zombie zombie : activeZombies) {
            int zX = (int) Math.round(zombie.getX());
            int zY = zombie.getY();
            if (zX >= 0 && zX < board.getColumns() && zY >= 0 && zY < board.getRows()) {
                board.getTile(zY, zX).setZombie(zombie);
            }
        }

        if (activeMiniGame instanceof Beghoul) {
            ((Beghoul) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof IZombie) {
            ((IZombie) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof Vasebreaker) {
            ((Vasebreaker) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof WallnutBowling) {
            ((WallnutBowling) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof Zombotany) {
            ((Zombotany) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        }

        SpecialLevelType specialType = level.getSpecialLevelType();
        if (activeMiniGame == null && specialType == SpecialLevelType.CONVEYOR_BELT) {
            if (tickCount == 1 || tickCount % 120 == 0) {
                String randomPlant = getRandomUnlockedPlant();
                conveyorBeltPlants.add(randomPlant);
            }
        }

        if (specialType == SpecialLevelType.SAVE_OUR_SEEDS) {
            for (Plant p : seedsToProtect) {
                if (!p.isAlive() || !activePlants.contains(p)) {
                    lost = true;
                    running = false;
                    gameLogMessages.add("The zombie ate your brain; LOSER!!!");
                    return;
                }
            }
        }

        if (specialType == SpecialLevelType.TIMED_WAR) {
            if (zombiesKilledInLevel >= level.getTargetZombiesToKill() || sunCount >= level.getTargetSunsToProduce()) {
                won = true;
                running = false;
                gameLogMessages.add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                if (model.UserSession.isLoggedIn() && model.UserSession.getCurrentUser() != null) {
                    model.UserSession.getCurrentUser().addNews("Congratulations! New levels and mini-games are now unlocked.");
                    util.FileManager.updateUser(model.UserSession.getCurrentUser());
                }
                return;
            }
            if (tickCount >= level.getTimeLimitTicks()) {
                lost = true;
                running = false;
                gameLogMessages.add("The zombie ate your brain; LOSER!!!");
                return;
            }
        }

        if (specialType == SpecialLevelType.DEAD_LINE) {
            int lineCol = level.getDeadlineColumn();
            for (Zombie z : activeZombies) {
                if (z.getX() <= lineCol) {
                    lost = true;
                    running = false;
                    gameLogMessages.add("The zombie ate your brain; LOSER!!!");
                    return;
                }
            }
        }

        if (specialType == SpecialLevelType.LOVE_YOUR_PLANTS) {
            if (plantsLostCount > level.getMaxPlantsLostAllowed()) {
                lost = true;
                running = false;
                gameLogMessages.add("The zombie ate your brain; LOSER!!!");
                return;
            }
        }

        updatePlantsAndAbilities();
        List<Zombie> zombiesToRemove = new ArrayList<>();
        List<Zombie> zombiesToAdd = new ArrayList<>();
        for (Zombie zombie : new ArrayList<>(activeZombies)) {
            zombie.updateEffects();
            zombie.updateCooldown();
            if (!zombie.isAlive()) {
                zombiesToRemove.add(zombie);
                if (zombie.getStolenSuns() > 0) {
                    int returnSun = zombie.getName().equalsIgnoreCase("ZombieCrystalSkull") ?
                            zombie.getStolenSuns() / 2 : zombie.getStolenSuns();
                    addSun(returnSun);
                    gameLogMessages.add("Ra/Turquoise Zombie died! Returned " + returnSun + " stolen suns.");
                }
                if (zombie.getName().equalsIgnoreCase("ZombieWizard")) {
                    for (Plant p : activePlants) {
                        if (p.isTransformedToSheep()) {
                            p.setTransformedToSheep(false);
                        }
                    }
                }
                String deathMessage = "Zombie of type " + zombie.getName() + " is dead at (" + (int) Math.round(zombie.getX()) + ", " + zombie.getY() + ")";
                gameLogMessages.add(deathMessage);

                processZombieDeathDrops(zombie);

                scoreGame.onZombieKilled(zombie, this);
                zombiesKilledInLevel++;
                continue;
            }

            processSpecialZombieAbilities(zombie, zombiesToAdd);
            if (!zombie.hasEffect(ZombieEffect.FROZEN)) {
                double zombieX = zombie.getX();
                int zombieY = zombie.getY();

                int targetPlantX = (int) Math.floor(zombieX);
                if (zombieX - targetPlantX == 0.0) {
                    targetPlantX = targetPlantX - 1;
                }

                Plant targetPlant = getPlantAt(targetPlantX, zombieY);
                if (targetPlant != null && !targetPlant.isBowlingBall() && zombieX - targetPlant.getX() <= 1.05) {
                    if (zombie.getName().equalsIgnoreCase("ZombieExplorer") && zombie.isTorchLit()) {
                        activePlants.remove(targetPlant);
                        board.getTile(targetPlant.getY(), targetPlant.getX()).setPlant(null);
                        plantsLostCount++;
                        gameLogMessages.add("Explorer Zombie burned plant " + targetPlant.getName() + " at (" + targetPlant.getX() + ", " + targetPlant.getY() + ")!");
                    } else if (zombie.getName().equalsIgnoreCase("ZombieModernAllStar") && zombie.isCharging()) {
                        targetPlant.takeDamage(1500);
                        zombie.setCharging(false);
                        gameLogMessages.add("All-Star Zombie tackled plant " + targetPlant.getName() + "!");
                        if (!targetPlant.isAlive()) {
                            activePlants.remove(targetPlant);
                            board.getTile(targetPlant.getY(), targetPlant.getX()).setPlant(null);
                            plantsLostCount++;
                        }
                    } else if (zombie.getName().equalsIgnoreCase("ZombieWizard")) {
                        if (!targetPlant.isTransformedToSheep()) {
                            targetPlant.setTransformedToSheep(true);
                            gameLogMessages.add("Wizard Zombie transformed plant at (" + targetPlant.getX() + ", " + targetPlant.getY() + ") into a sheep!");
                        }
                    } else {
                        if (tickCount % 10 == 0) {
                            targetPlant.takeDamage(zombie.getDamage());
                            scoreGame.onDamageTaken(zombie.getDamage());
                            if (!targetPlant.isAlive()) {
                                activePlants.remove(targetPlant);
                                board.getTile(targetPlant.getY(), targetPlant.getX()).setPlant(null);
                                plantsLostCount++;
                                gameLogMessages.add("Plant " + targetPlant.getName() + " at (" + targetPlant.getX() + ", " + targetPlant.getY() + ") is destroyed.");
                                if (activeMiniGame instanceof Beghoul) {
                                    ((Beghoul) activeMiniGame).createCrater(targetPlant.getY(), targetPlant.getX());
                                }
                            }
                        }
                    }
                } else {
                    int nextTileX = (int) Math.floor(zombieX);
                    if (nextTileX >= 0 && nextTileX < board.getColumns() && zombieY >= 0 && zombieY < board.getRows()) {
                        Tile currentTile = board.getTile(zombieY, nextTileX);
                        if (currentSeason != null && "FrostbiteCaves".equalsIgnoreCase(currentSeason.getName()) && currentTile != null && currentTile.isSlideway() && !zombie.isDodoRider()) {
                            int targetRow = zombie.getY() + currentTile.getSlideRowOffset();
                            if (targetRow >= 0 && targetRow < board.getRows()) {
                                zombie.setY(targetRow);
                                zombie.move();
                            } else {
                                zombie.move();
                            }
                        } else {
                            zombie.move();
                        }
                    } else {
                        zombie.move();
                    }
                }
            }

            if (zombie.getX() <= 0) {
                int row = zombie.getY();
                if (activeMiniGame instanceof IZombie) {
                    IZombie iz = (IZombie) activeMiniGame;
                    iz.eatBrain(row);
                    zombiesToRemove.add(zombie);
                    if (iz.isVictoryConditionMet()) {
                        won = true;
                        running = false;
                        gameLogMessages.add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                        return;
                    }
                    continue;
                }

                if (!(activeMiniGame instanceof WallnutBowling)) {
                    LawnMower mower = lawnMowers[row];
                    if (!mower.isUsed()) {
                        List<Zombie> toKill = new ArrayList<>();
                        for (Zombie z : activeZombies) {
                            if (z.getY() == row) {
                                toKill.add(z);
                            }
                        }
                        if (!toKill.isEmpty()) {
                            mower.activate();
                            gameLogMessages.add("The lawn mower in the row " + row + " is triggered and killed these zombies:");
                            zombiesToRemove.addAll(toKill);
                            for (Zombie killed : toKill) {
                                scoreGame.onZombieKilled(killed, this);
                                zombiesKilledInLevel++;
                                gameLogMessages.add("Zombie of type " + killed.getName() + " is dead at (" + (int)Math.round(killed.getX()) + ", " + killed.getY() + ")");
                            }
                        }
                    } else {
                        if (!zombiesToRemove.contains(zombie)) {
                            lost = true;
                            running = false;
                            scoreGame.onComboBreak();
                            gameLogMessages.add("The zombie ate your brain; LOSER!!!");
                            return;
                        }
                    }
                }
            }
        }
        activeZombies.removeAll(zombiesToRemove);
        activeZombies.addAll(zombiesToAdd);

        if (lost || won || !running) return;
        if (spawner != null) {
            if ((specialType == SpecialLevelType.PLANT_WHAT_YOU_GET && !zombieWavesStarted) || activeMiniGame instanceof Vasebreaker || activeMiniGame instanceof IZombie || activeMiniGame instanceof Beghoul) {
            } else {
                Zombie newlySpawned = spawner.update();
                if (newlySpawned != null) {
                    if (currentSeason != null && "AncientEgypt".equalsIgnoreCase(currentSeason.getName()) && spawner.isFinalWave()) {
                        int currentWave = spawner.getCurrentWave();
                        int totalWaves = spawner.getTotalWaves();
                        int defaultColumn = board.getColumns() - 1;
                        int modifiedCol = currentSeason.modifySpawnColumn(currentWave, totalWaves, defaultColumn, spawner.getZombiesSpawnedInWave(), board, newlySpawned.getY());
                        newlySpawned.setX(modifiedCol);
                    }
                    activeZombies.add(newlySpawned);
                    int cost = newlySpawned.getWaveCost();
                    gameLogMessages.add("Zombie " + newlySpawned.getName() + " spawned at wave " + spawner.getCurrentWave() + " in lane " + newlySpawned.getY() + " which costed " + cost + ".");
                }
            }
        }
        if (lost || won || !running) return;
        handleSunDrop();
        if (currentSeason != null) {
            currentSeason.handleTick(this);
            if (currentSeason.getName().equalsIgnoreCase("BigWaveBeach")) {
                for (Plant p : new ArrayList<>(activePlants)) {
                    Tile t = board.getTile(p.getY(), p.getX());
                    if (t != null && t.getType() == TileType.WATER) {
                        boolean isAquatic = p.isAquatic();
                        boolean hasLilyPad = (t.getSupportPlant() != null && t.getSupportPlant().getName().equalsIgnoreCase("Lily Pad"));
                        if (!isAquatic && !hasLilyPad) {
                            activePlants.remove(p);
                            t.setPlant(null);
                            gameLogMessages.add("Plant " + p.getName() + " drowned in the rising tide!");
                        }
                    }
                }
            }
        }
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                board.getTile(r, c).setZombie(null);
            }
        }
        for (Zombie zombie : activeZombies) {
            int zX = (int) Math.round(zombie.getX());
            int zY = zombie.getY();
            if (zX >= 0 && zX < board.getColumns() && zY >= 0 && zY < board.getRows()) {
                board.getTile(zY, zX).setZombie(zombie);
            }
        }
        board.updateProjectilesAndCollisions(this);
        if (spawner != null && !(activeMiniGame instanceof Vasebreaker) && !(activeMiniGame instanceof IZombie) && !(activeMiniGame instanceof Beghoul)) {
            if (spawner.isWaveComplete() && activeZombies.isEmpty()) {
                if (spawner.getCurrentWave() < spawner.getTotalWaves()) {
                    int nextWave = spawner.getCurrentWave() + 1;
                    if (currentSeason != null) { currentSeason.handleWaveStart(this); }
                    spawner.startWave(nextWave);
                    if (spawner.isFinalWave()) {
                        gameLogMessages.add("The final wave has come.");
                    } else {
                        gameLogMessages.add("Wave " + nextWave + " started.");
                    }
                } else {
                    won = true;
                    running = false;
                    scoreGame.onWaveCompleted(spawner.getCurrentWave());
                    gameLogMessages.add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                    if (model.UserSession.isLoggedIn() && model.UserSession.getCurrentUser() != null) {
                        model.UserSession.getCurrentUser().addNews("Congratulations! New levels and mini-games are now unlocked.");
                        util.FileManager.updateUser(model.UserSession.getCurrentUser());
                    }
                }
            } else if (!spawner.isWaveComplete()) {
                double healthSum = 0;
                double maxHealthSum = 0;
                for (Zombie z : activeZombies) {
                    healthSum += z.getHealth();
                    maxHealthSum += z.getMaxHealth();
                }
                int scheduledCount = spawner.getZombiesInWave();
                int remainingToSpawn = scheduledCount - spawner.getZombiesSpawnedInWave();
                double remainingSpawnHealth = remainingToSpawn * 200.0;
                double totalWaveHealth = (scheduledCount * 200.0) + maxHealthSum;
                double currentWaveHealth = healthSum + remainingSpawnHealth;
                if (totalWaveHealth > 0 && (currentWaveHealth / totalWaveHealth) <= 0.25) {
                    if (spawner.getCurrentWave() < spawner.getTotalWaves()) {
                        int nextWave = spawner.getCurrentWave() + 1;
                        if (currentSeason != null) { currentSeason.handleWaveStart(this); }
                        spawner.startWave(nextWave);
                        if (spawner.isFinalWave()) {
                            gameLogMessages.add("The final wave has come.");
                        } else {
                            gameLogMessages.add("Wave " + nextWave + " started.");
                        }
                    }
                }
            }
        }
    }

    private void updatePlantsAndAbilities() {
        List<Plant> plantsToRemove = new ArrayList<>();
        for (Plant plant : new ArrayList<>(activePlants)) {
            if (plant.isFrozen() || plant.isBowlingBall() || plant.isTransformedToSheep()) {
                continue;
            }
            plant.update();
            String name = plant.getName();
            if ("SUN_PRODUCER".equalsIgnoreCase(plant.getCategory())) {
                if (name.equalsIgnoreCase("Gold Bloom")) {
                    addSun((int) plant.getAbilityValue());
                    plantsToRemove.add(plant);
                    gameLogMessages.add("Gold Bloom burst and produced " + (int) plant.getAbilityValue() + " suns!");
                } else if (name.equalsIgnoreCase("Enlighten-mint")) {
                    triggerMintBoost("SUN_PRODUCER");
                    plantsToRemove.add(plant);
                    gameLogMessages.add("Enlighten-mint boosted all Sun Producers!");
                } else {
                    int intervalTicks = (int) (plant.getActionInterval() * 10);
                    if (intervalTicks > 0 && plant.shouldShoot()) {
                        if (!plant.isHasSunToCollect()) {
                            plant.setHasSunToCollect(true);
                            gameLogMessages.add("Plant " + name + " produced sun at (" + plant.getX() + ", " + plant.getY() + ")");
                        }
                    }
                }
            }
            boolean isAttacker = "SHOOTER".equalsIgnoreCase(plant.getCategory()) || "STRIKE_THROUGH".equalsIgnoreCase(plant.getCategory()) || "HOMING".equalsIgnoreCase(plant.getCategory()) || "LOBBER".equalsIgnoreCase(plant.getCategory());
            if (isAttacker && plant.shouldShoot()) {
                boolean targetInRow = hasZombieInRow(plant.getY()) || board.hasGraveInRow(plant.getY());

                if (name.equalsIgnoreCase("Threepeater")) {
                    int py = plant.getY();
                    targetInRow = hasZombieInRow(py) || board.hasGraveInRow(py)
                            || (py > 0 && (hasZombieInRow(py - 1) || board.hasGraveInRow(py - 1)))
                            || (py < board.getRows() - 1 && (hasZombieInRow(py + 1) || board.hasGraveInRow(py + 1)));
                }

                boolean globalTarget = !activeZombies.isEmpty();
                if (targetInRow || ("HOMING".equalsIgnoreCase(plant.getCategory()) && globalTarget) || name.equalsIgnoreCase("Starfruit") || name.equalsIgnoreCase("Laser Bean")) {
                    spawnBulletsForPlant(plant);
                }
            }
            if ("MELEE".equalsIgnoreCase(plant.getCategory()) && plant.shouldShoot()) {
                executeMeleeAttack(plant);
            }
            if ("EXPLOSIVE".equalsIgnoreCase(plant.getCategory())) {
                executeExplosiveLogic(plant, plantsToRemove);
            }

            if (("MODIFIER".equalsIgnoreCase(plant.getCategory()) || "HOMING".equalsIgnoreCase(plant.getCategory())) && plant.shouldShoot()) {
                executeUtilityLogic(plant, plantsToRemove);
            }
            if (name.contains("-mint") && !"Enlighten-mint".equalsIgnoreCase(name)) {
                executeMintLogic(plant, plantsToRemove);
            }
        }
        for (Plant p : plantsToRemove) {
            removePlant(p);
            Tile t = board.getTile(p.getY(), p.getX());
            if (t != null && t.getPlant() == p) {
                t.setPlant(null);
            }
        }
    }

    private void spawnBulletsForPlant(Plant plant) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        int dmg = plant.getDamage() > 0 ? plant.getDamage() : 20;

        if (name.equalsIgnoreCase("Peashooter") || name.equalsIgnoreCase("Pea Pod")) {
            int heads = plant.getPeaPodHeads();
            for (int i = 0; i < heads; i++) {
                bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
        } else if (name.equalsIgnoreCase("Repeater")) {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
        } else if (name.equalsIgnoreCase("Threepeater")) {
            for (int r = Math.max(0, py - 1); r <= Math.min(board.getRows() - 1, py + 1); r++) {
                bullets.add(new Bullet(dmg, r, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
        } else if (name.equalsIgnoreCase("Snow Pea")) {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.ICE, false, false, 0));
        } else if (name.equalsIgnoreCase("Rotobaga")) {
            bullets.add(new Bullet(dmg, Math.max(0, py - 1), px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            bullets.add(new Bullet(dmg, Math.min(board.getRows() - 1, py + 1), px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            bullets.add(new Bullet(dmg, Math.max(0, py - 1), Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
            bullets.add(new Bullet(dmg, Math.min(board.getRows() - 1, py + 1), Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
        } else if (name.equalsIgnoreCase("Split Pea")) {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            bullets.add(new Bullet(dmg, py, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
            bullets.add(new Bullet(dmg, py, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
        } else if (name.equalsIgnoreCase("Citron")) {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.LASER, true, false, 0));
        } else if (name.equalsIgnoreCase("Bowling Bulb")) {
            int bulbDmg = 40;
            if (plant.getHitCount() % 3 == 1) bulbDmg = 120;
            else if (plant.getHitCount() % 3 == 2) bulbDmg = 180;
            plant.incrementHitCount();
            bullets.add(new Bullet(bulbDmg, py, px + 1, Bullet.BulletType.NORMAL, true, true, 1));
        } else if (name.equalsIgnoreCase("Cactus")) {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.STRIKE_THROUGH, true, false, 0));
        } else if (name.equalsIgnoreCase("Fire Peashooter")) {
            bullets.add(new Bullet(40 + (plant.getLevel() >= 2 ? 10 : 0), py, px + 1, Bullet.BulletType.FIRE, false, false, 0));
            // ذوب کردن یخ زامبی در صورت حضور
            Zombie z = getFirstZombieInRowAhead(py, px);
            if (z != null && (z.hasEffect(ZombieEffect.FROZEN) || z.hasEffect(ZombieEffect.CHILLED))) {
                z.removeEffect(ZombieEffect.FROZEN);
                z.removeEffect(ZombieEffect.CHILLED);
                gameLogMessages.add("Fire Peashooter melted ice on zombie in lane " + py);
            }
        } else if (name.equalsIgnoreCase("Starfruit")) {
            // شلیک ۵ جهته (جلو، بالا، پایین، عقب-بالا، عقب-پایین)
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            if (py > 0) bullets.add(new Bullet(dmg, py - 1, px, Bullet.BulletType.NORMAL, false, false, 0));
            if (py < board.getRows() - 1) bullets.add(new Bullet(dmg, py + 1, px, Bullet.BulletType.NORMAL, false, false, 0));
            if (py > 0) bullets.add(new Bullet(dmg, py - 1, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
            if (py < board.getRows() - 1) bullets.add(new Bullet(dmg, py + 1, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
        } else if (name.equalsIgnoreCase("Laser Bean")) {
            // شلیک پرتو لیزر نافذ کل لاین
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.LASER, true, false, 0));
            gameLogMessages.add("Laser Bean fired a laser beam down row " + py);
        } else if (name.equalsIgnoreCase("Mega Gatling Pea")) {
            for (int i = 0; i < 4; i++) {
                bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
        } else if (name.equalsIgnoreCase("Fume-shroom")) {
            bullets.add(new Bullet(30, py, px + 1, Bullet.BulletType.STRIKE_THROUGH, true, false, 0));
        } else if (name.equalsIgnoreCase("Goo Peashooter")) {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.POISON, false, false, 0));
        } else if (name.toLowerCase().contains("pult") || name.toLowerCase().contains("melon")) {
            Bullet.BulletType bType = name.toLowerCase().contains("winter") ? Bullet.BulletType.ICE : Bullet.BulletType.LOB;
            int pDmg = name.toLowerCase().contains("melon") ? 80 : 40;
            bullets.add(new Bullet(pDmg, py, px + 1, bType, false, name.toLowerCase().contains("melon"), 1));
        } else {
            bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
        }
    }

    public String applyPlantFood(Plant plant) {
        if (plant == null) return "";
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        int dmg = plant.getDamage() > 0 ? plant.getDamage() : 20;

        if (name.equalsIgnoreCase("Sunflower")) {
            addSun(150);
            gameLogMessages.add("Sunflower generated 150 suns with Plant Food!");
            return "Plant Food Effect: Produced 150 suns!";
        } else if (name.equalsIgnoreCase("Twin Sunflower")) {
            addSun(250);
            gameLogMessages.add("Twin Sunflower generated 250 suns with Plant Food!");
            return "Plant Food Effect: Produced 250 suns!";
        } else if (name.equalsIgnoreCase("Sun-shroom")) { // شناسه ۳
            plant.setPlantStage(3);
            addSun(225);
            gameLogMessages.add("Sun-shroom instantly grew to max size and generated 225 suns!");
            return "Plant Food Effect: Grew to max size and produced 225 suns!";
        } else if (name.equalsIgnoreCase("Primal Sunflower")) {
            addSun(225);
            gameLogMessages.add("Primal Sunflower generated 225 suns with Plant Food!");
            return "Plant Food Effect: Produced 225 suns!";
        } else if (name.equalsIgnoreCase("Gold Bloom")) {
            addSun(375);
            gameLogMessages.add("Gold Bloom generated 375 suns with Plant Food!");
            return "Plant Food Effect: Produced 375 suns!";
        } else if (name.equalsIgnoreCase("Peashooter")) {
            for (int i = 0; i < 60; i++) {
                bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            gameLogMessages.add("Peashooter unleashed a Gatling volley of 60 peas!");
            return "Plant Food Effect: Unleashed Gatling barrage!";
        } else if (name.equalsIgnoreCase("Repeater")) {
            for (int i = 0; i < 60; i++) {
                bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            bullets.add(new Bullet(400, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            gameLogMessages.add("Repeater unleashed heavy barrage + giant pea (400 dmg)!");
            return "Plant Food Effect: Unleashed heavy barrage & giant pea!";
        } else if (name.equalsIgnoreCase("Threepeater")) {
            for (int r = Math.max(0, py - 1); r <= Math.min(board.getRows() - 1, py + 1); r++) {
                for (int i = 0; i < 20; i++) {
                    bullets.add(new Bullet(dmg, r, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
                }
            }
            gameLogMessages.add("Threepeater unleashed fan barrage across 3 lanes!");
            return "Plant Food Effect: Unleashed fan barrage across 3 lanes!";
        } else if (name.equalsIgnoreCase("Snow Pea")) {
            for (int i = 0; i < 60; i++) {
                bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.ICE, false, false, 0));
            }
            for (Zombie z : activeZombies) {
                if (z.getY() == py) {
                    z.applyFrozen(5.0);
                }
            }
            gameLogMessages.add("Snow Pea unleashed ice barrage and froze all zombies in lane " + py + "!");
            return "Plant Food Effect: Unleashed ice barrage and froze the lane!";
        } else if (name.equalsIgnoreCase("Rotobaga")) {
            for (int i = 0; i < 15; i++) {
                bullets.add(new Bullet(dmg, Math.max(0, py - 1), px + 1, Bullet.BulletType.NORMAL, false, false, 0));
                bullets.add(new Bullet(dmg, Math.min(board.getRows() - 1, py + 1), px + 1, Bullet.BulletType.NORMAL, false, false, 0));
                bullets.add(new Bullet(dmg, Math.max(0, py - 1), Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
                bullets.add(new Bullet(dmg, Math.min(board.getRows() - 1, py + 1), Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
            }
            gameLogMessages.add("Rotobaga unleashed rapid diagonal barrage!");
            return "Plant Food Effect: Unleashed rapid diagonal barrage!";
        } else if (name.equalsIgnoreCase("Pea Pod")) {
            int heads = plant.getPeaPodHeads();
            for (int i = 0; i < heads; i++) {
                bullets.add(new Bullet(400, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            gameLogMessages.add("Pea Pod fired giant peas for each head!");
            return "Plant Food Effect: Fired giant peas for each head!";
        } else if (name.equalsIgnoreCase("Split Pea")) {
            for (int i = 0; i < 30; i++) {
                bullets.add(new Bullet(dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            for (int i = 0; i < 60; i++) {
                bullets.add(new Bullet(dmg, py, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
            }
            gameLogMessages.add("Split Pea unleashed rapid barrage forward and backward!");
            return "Plant Food Effect: Rapid barrage forward and backward!";
        } else if (name.equalsIgnoreCase("Citron")) {
            for (Zombie z : activeZombies) {
                if (z.getY() == py) {
                    z.takeDamage(5000, true);
                }
            }
            gameLogMessages.add("Citron fired a massive plasma ball, clearing the entire lane!");
            return "Plant Food Effect: Fired massive plasma ball!";
        } else if (name.equalsIgnoreCase("Caulipower")) {
            int count = 0;
            for (Zombie z : activeZombies) {
                if (!z.isHypnotized()) {
                    z.setHypnotized(true);
                    count++;
                    if (count >= 3) break;
                }
            }
            gameLogMessages.add("Caulipower hypnotized " + count + " random zombies!");
            return "Plant Food Effect: Hypnotized multiple zombies!";
        } else if (name.equalsIgnoreCase("Electric Blueberry")) {
            int count = 0;
            for (Zombie z : new ArrayList<>(activeZombies)) {
                z.takeDamage(5000, true);
                count++;
                if (count >= 3) break;
            }
            gameLogMessages.add("Electric Blueberry zapped and destroyed " + count + " zombies!");
            return "Plant Food Effect: Instantly zapped " + count + " zombies!";
        } else if (name.equalsIgnoreCase("Bowling Bulb")) {
            for (int i = 0; i < 3; i++) {
                bullets.add(new Bullet(400, py, px + 1, Bullet.BulletType.NORMAL, true, true, 2));
            }
            gameLogMessages.add("Bowling Bulb launched 3 giant explosive bulbs!");
            return "Plant Food Effect: Launched 3 giant explosive bulbs!";
        } else if (name.equalsIgnoreCase("Cactus")) {
            bullets.add(new Bullet(150, py, px + 1, Bullet.BulletType.STRIKE_THROUGH, true, false, 0));
            gameLogMessages.add("Cactus fired electric thorns with infinite penetration!");
            return "Plant Food Effect: Fired electric thorns with infinite penetration!";
        } else if (name.equalsIgnoreCase("Starfruit")) { // شناسه ۱۸
            for (int i = 0; i < 10; i++) {
                bullets.add(new Bullet(100, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0));
                if (py > 0) bullets.add(new Bullet(100, py - 1, px, Bullet.BulletType.NORMAL, false, false, 0));
                if (py < board.getRows() - 1) bullets.add(new Bullet(100, py + 1, px, Bullet.BulletType.NORMAL, false, false, 0));
                if (py > 0) bullets.add(new Bullet(100, py - 1, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
                if (py < board.getRows() - 1) bullets.add(new Bullet(100, py + 1, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0));
            }
            gameLogMessages.add("Starfruit unleashed giant star barrage in 5 directions!");
            return "Plant Food Effect: Unleashed giant star barrage in 5 directions!";
        } else if (name.equalsIgnoreCase("Fire Peashooter")) { // شناسه ۱۹
            for (Zombie z : activeZombies) {
                if (z.getY() == py) {
                    z.takeDamage(500, true);
                    z.removeEffect(ZombieEffect.FROZEN);
                    z.removeEffect(ZombieEffect.CHILLED);
                }
            }
            gameLogMessages.add("Fire Peashooter scorched the entire row with a trail of fire!");
            return "Plant Food Effect: Scorched the entire row with fire!";
        } else if (name.equalsIgnoreCase("Laser Bean")) { // شناسه ۲۰
            for (Zombie z : activeZombies) {
                if (z.getY() == py) {
                    z.takeDamage(1800, true);
                }
            }
            gameLogMessages.add("Laser Bean unleashed a massive laser beam dealing 1800 damage to all zombies in row!");
            return "Plant Food Effect: Unleashed massive laser beam!";
        }
        return "Plant Food applied!";
    }

    private void executeMeleeAttack(Plant plant) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        if (name.equalsIgnoreCase("Chomper")) {
            if (!plant.isDigesting()) {
                Zombie target = getFirstZombieInRowAhead(py, px);
                if (target != null && target.getX() - px <= 1.2) {
                    target.takeDamage(99999, true);
                    plant.startDigestion(400);
                    gameLogMessages.add("Chomper swallowed a zombie at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Bonk Choy") || name.equalsIgnoreCase("Wasabi Whip")) {
            for (Zombie z : activeZombies) {
                if (z.getY() == py && Math.abs(z.getX() - px) <= 1.1) {
                    z.takeDamage(plant.getDamage(), false);
                }
            }
        } else if (name.equalsIgnoreCase("Phat Beet") || name.equalsIgnoreCase("Kiwibeast")) {
            int radius = plant.getPlantStage();
            for (Zombie z : activeZombies) {
                if (Math.abs(z.getY() - py) <= radius && Math.abs(z.getX() - px) <= radius) {
                    z.takeDamage(plant.getDamage(), false);
                }
            }
        }
    }

    private void executeExplosiveLogic(Plant plant, List<Plant> toRemove) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        if (name.equalsIgnoreCase("Cherry Bomb") || name.equalsIgnoreCase("Grapeshot")) {
            for (Zombie z : activeZombies) {
                if (Math.abs(z.getY() - py) <= 1 && Math.abs(z.getX() - px) <= 1.5) {
                    z.takeDamage(1800, true);
                }
            }
            toRemove.add(plant);
            gameLogMessages.add(name + " exploded in a 3x3 area!");
        } else if (name.equalsIgnoreCase("Jalapeno")) {
            for (Zombie z : activeZombies) {
                if (z.getY() == py) {
                    z.takeDamage(1800, true);
                }
            }
            toRemove.add(plant);
            gameLogMessages.add("Jalapeno incinerated row " + py + "!");
        } else if (name.equalsIgnoreCase("Doom-shroom")) {
            for (Zombie z : activeZombies) {
                z.takeDamage(1800, true);
            }
            toRemove.add(plant);
            gameLogMessages.add("Doom-shroom wiped out the entire field!");
        } else if (name.equalsIgnoreCase("Potato Mine") || name.equalsIgnoreCase("Primal Potato Mine")) {
            if (plant.isArmed()) {
                Zombie target = getFirstZombieInRowAhead(py, px - 0.5);
                if (target != null && Math.abs(target.getX() - px) <= 0.6) {
                    int radius = name.contains("Primal") ? 1 : 0;
                    for (Zombie z : activeZombies) {
                        if (Math.abs(z.getY() - py) <= radius && Math.abs(z.getX() - px) <= (radius + 0.5)) {
                            z.takeDamage(plant.getDamage(), true);
                        }
                    }
                    toRemove.add(plant);
                    gameLogMessages.add(name + " detonated at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Squash")) {
            Zombie target = getFirstZombieInRowAhead(py, px - 0.5);
            if (target != null && Math.abs(target.getX() - px) <= 1.2) {
                target.takeDamage(1800, true);
                toRemove.add(plant);
                gameLogMessages.add("Squash squashed zombie at (" + px + ", " + py + ")!");
            }
        } else if (name.equalsIgnoreCase("Ice-shroom") || name.equalsIgnoreCase("Iceberg Lettuce")) {
            if (name.equalsIgnoreCase("Ice-shroom")) {
                for (Zombie z : activeZombies) {
                    z.applyFrozen(5.0);
                }
                toRemove.add(plant);
                gameLogMessages.add("Ice-shroom froze all zombies!");
            } else {
                Zombie z = getFirstZombieInRowAhead(py, px - 0.5);
                if (z != null && Math.abs(z.getX() - px) <= 0.5) {
                    z.applyFrozen(5.0);
                    toRemove.add(plant);
                    gameLogMessages.add("Iceberg Lettuce froze zombie at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Hot Potato")) {
            Tile tile = board.getTile(py, px);
            if (tile != null && tile.getPlant() != null && tile.getPlant().isFrozen()) {
                tile.getPlant().melt();
                gameLogMessages.add("Hot Potato melted ice at (" + px + ", " + py + ")!");
            }
            toRemove.add(plant);
        } else if (name.equalsIgnoreCase("Grave Buster")) {
            Tile tile = board.getTile(py, px);
            if (tile != null && tile.getType() == TileType.GRAVE) {
                board.removeGrave(py, px);
                gameLogMessages.add("Grave Buster destroyed grave at (" + px + ", " + py + ")!");
            }
            toRemove.add(plant);
        }
    }

    private void executeUtilityLogic(Plant plant, List<Plant> toRemove) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        if (name.equalsIgnoreCase("Magnet-shroom") && plant.getMagnetCooldownTicks() <= 0) {
            for (Zombie z : activeZombies) {
                if (z.getArmorHealth() > 0 && ("BUCKET".equalsIgnoreCase(z.getArmorType()) || "CONE".equalsIgnoreCase(z.getArmorType()) || "KNIGHT".equalsIgnoreCase(z.getArmorType()))) {
                    z.setArmorHealth(0);
                    z.setArmorType("none");
                    plant.startMagnetCooldown(150);
                    gameLogMessages.add("Magnet-shroom removed armor from zombie at lane " + z.getY() + "!");
                    break;
                }
            }
        } else if (name.equalsIgnoreCase("Caulipower") || name.equalsIgnoreCase("Electric Blueberry")) {
            if (!activeZombies.isEmpty()) {
                Zombie target = null;
                if (name.equalsIgnoreCase("Electric Blueberry") && plant.getLevel() >= 3) {
                    for (Zombie z : activeZombies) {
                        if (target == null || z.getHealth() > target.getHealth()) {
                            target = z;
                        }
                    }
                } else {
                    target = activeZombies.get(new Random().nextInt(activeZombies.size()));
                }

                if (target != null) {
                    if (name.equalsIgnoreCase("Caulipower")) {
                        target.setHypnotized(true);
                        gameLogMessages.add("Caulipower hypnotized zombie " + target.getName() + "!");
                    } else {
                        target.takeDamage(5000, true);
                        gameLogMessages.add("Electric Blueberry zapped zombie " + target.getName() + "!");
                    }
                }
            }
        }
    }

    private void executeMintLogic(Plant plant, List<Plant> toRemove) {
        String name = plant.getName();
        String familyCategory = switch (name) {
            case "Appease-mint" -> "SHOOTER";
            case "Arma-mint" -> "LOBBER";
            case "Bombard-mint" -> "EXPLOSIVE";
            case "Enforce-mint" -> "MELEE";
            case "Reinforce-mint" -> "WALL_NUT";
            case "Enchant-mint" -> "MODIFIER";
            case "Pierce-mint" -> "STRIKE_THROUGH";
            case "catTail-mint" -> "HOMING";
            default -> "";
        };
        if (!familyCategory.isEmpty()) {
            triggerMintBoost(familyCategory);
            toRemove.add(plant);
            gameLogMessages.add(name + " activated Plant Food boost for all " + familyCategory + " plants!");
        }
    }

    private void triggerMintBoost(String category) {
        for (Plant p : activePlants) {
            if (category.equalsIgnoreCase(p.getCategory())) {
                if ("WALL_NUT".equalsIgnoreCase(category)) {
                    p.applyPlantFoodArmor(4000);
                } else {
                    p.heal(p.getMaxHealth());
                }
            }
        }
    }

    private void processZombieDeathDrops(Zombie zombie) {
        Random r = new Random();
        if (zombie.isGlowing()) {
            if (getPlantFoodCount() < 3) {
                addPlantFood();
                gameLogMessages.add("The glowing zombie dropped a plant food; you have " + getPlantFoodCount() + " plant foods now.");
            }
        }
        if (r.nextInt(100) < 10) {
            int dropType = r.nextInt(3);
            if (dropType == 0) {
                addCoins(50);
                gameLogMessages.add("A zombie dropped a coin; you have " + getCoins() + " coins now.");
            } else if (dropType == 1) {
                addDiamonds(1);
                gameLogMessages.add("A zombie dropped a diamond; you have " + getDiamonds() + " diamonds now.");
            } else {
                if (getGreenhouse() != null) {
                    getGreenhouse().addPot(new model.greenhouse.Pot(0, 0));
                }
                int potCount = getGreenhouse() != null ? getGreenhouse().getUnlockedPotCount() : 1;
                gameLogMessages.add("A zombie dropped a pot; you have " + potCount + " pots now.");
            }
        }
    }

    private void processSpecialZombieAbilities(Zombie zombie, List<Zombie> zombiesToAdd) {
        String name = zombie.getName();
        if (name.equalsIgnoreCase("ZombieGargantuar") && !zombie.isHasThrownImp()) {
            if (zombie.getHealth() <= zombie.getMaxHealth() / 2) {
                zombie.setHasThrownImp(true);
                Zombie imp = model.entities.zombie.factory.ZombieFactory.createZombie("ZombieImp", difficultyLevel);
                if (imp != null) {
                    imp.setY(zombie.getY());
                    imp.setX(2.0);
                    zombiesToAdd.add(imp);
                    gameLogMessages.add("Gargantuar threw an Imp to column 2!");
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieRa")) {
            zombie.incrementRaStealTimer();
            if (zombie.getRaStealTimer() >= 20) {
                zombie.resetRaStealTimer();
                if (!suns.isEmpty()) {
                    Sun targetSun = suns.remove(0);
                    zombie.setStolenSuns(zombie.getStolenSuns() + targetSun.getValue());
                    gameLogMessages.add("Ra Zombie absorbed a sun from position (" + targetSun.getColumn() + ", " + targetSun.getRow() + ")!");
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieTombRaiser")) {
            zombie.incrementTombraiserTimer();
            if (zombie.getTombraiserTimer() >= 100) {
                zombie.resetTombraiserTimer();
                Random r = new Random();
                int rx = r.nextInt(board.getColumns());
                int ry = r.nextInt(board.getRows());
                Tile tile = board.getTile(ry, rx);
                if (tile != null && tile.isEmpty() && tile.getType() == TileType.GRASS) {
                    board.setupGrave(ry, rx, 700, 0, false);
                    gameLogMessages.add("Tombraiser Zombie created a grave at (" + rx + ", " + ry + ")");
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieIceAgeHunter")) {
            if (tickCount % 30 == 0) {
                Plant p = getFirstPlantInRow(zombie.getY());
                if (p != null) {
                    p.setFreezeLevel(p.getFreezeLevel() + 1);
                    gameLogMessages.add("Hunter Zombie threw a snowball at plant " + p.getName() + "!");
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieBeachFisherman")) {
            zombie.incrementFishermanTimer();
            if (zombie.getFishermanTimer() >= 25) {
                zombie.resetFishermanTimer();
                Plant target = getFirstPlantInRow(zombie.getY());
                if (target != null) {
                    if (target.getX() + 1 == (int) Math.round(zombie.getX())) {
                        activePlants.remove(target);
                        board.getTile(target.getY(), target.getX()).setPlant(null);
                        gameLogMessages.add("Fisherman Zombie hooked and destroyed plant " + target.getName() + "!");
                    } else if (target.getX() + 1 < board.getColumns()) {
                        board.getTile(target.getY(), target.getX()).setPlant(null);
                        target.setX(target.getX() + 1);
                        board.getTile(target.getY(), target.getX()).setPlant(target);
                        gameLogMessages.add("Fisherman Zombie pulled plant " + target.getName() + " to column " + target.getX());
                    }
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieBeachOctopus")) {
            zombie.incrementOctopusTimer();
            if (zombie.getOctopusTimer() >= 40) {
                zombie.resetOctopusTimer();
                Plant p = getFirstPlantInRow(zombie.getY());
                if (p != null && !p.isFrozen()) {
                    p.setFreezeLevel(3);
                    gameLogMessages.add("Octopus Zombie threw an octopus on plant " + p.getName() + "!");
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieDarkKing")) {
            zombie.incrementKingTimer();
            if (zombie.getKingTimer() >= 25) {
                zombie.resetKingTimer();
                for (Zombie neighbor : activeZombies) {
                    if (Math.abs(neighbor.getY() - zombie.getY()) <= 1 && Math.abs((int) neighbor.getX() - (int) zombie.getX()) <= 2) {
                        if (neighbor.getName().equalsIgnoreCase("ZombieDefault") || neighbor.getName().equalsIgnoreCase("NormalZombie")) {
                            neighbor.setArmorHealth(1600);
                            neighbor.setArmorType("KNIGHT");
                            gameLogMessages.add("King Zombie knighted a zombie at lane " + neighbor.getY() + "!");
                            break;
                        }
                    }
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieCrystalSkull")) {
            zombie.incrementTurquoiseLaserTimer();
            if (zombie.getTurquoiseLaserTimer() >= 50) {
                zombie.resetTurquoiseLaserTimer();
                int startCol = (int) Math.floor(zombie.getX());
                for (int c = startCol; c >= Math.max(0, startCol - 4); c--) {
                    Plant p = getPlantAt(c, zombie.getY());
                    if (p != null) {
                        activePlants.remove(p);
                        board.getTile(p.getY(), p.getX()).setPlant(null);
                        gameLogMessages.add("Turquoise Zombie fired laser and destroyed plant " + p.getName() + " at column " + c);
                    }
                }
            }
        }
        if (name.equalsIgnoreCase("ZombieProspector") && zombie.getDynamiteTimer() > 0) {
            if (zombie.getDynamiteTimer() <= 1.0) {
                zombie.setDynamiteTimer(0.0);
                zombie.setX(0.0);
                zombie.setAngry(true);
                gameLogMessages.add("Prospector Zombie's dynamite exploded! Flew to end of row.");
            }
        }
        if (name.equalsIgnoreCase("ZombiePiano")) {
            zombie.incrementPianoPlayTimer();
            if (zombie.getPianoPlayTimer() >= 30) {
                zombie.resetPianoPlayTimer();
                Random r = new Random();
                for (Zombie z : activeZombies) {
                    if (!z.isBoss() && r.nextBoolean()) {
                        int newY = z.getY() + (r.nextBoolean() ? 1 : -1);
                        if (newY >= 0 && newY < board.getRows()) {
                            z.setY(newY);
                        }
                    }
                }
                gameLogMessages.add("Piano Zombie played music! Zombies swapped lanes!");
            }
        }
    }

    private Plant getFirstPlantInRow(int row) {
        Plant closest = null;
        for (Plant p : activePlants) {
            if (p.getY() == row) {
                if (closest == null || p.getX() > closest.getX()) {
                    closest = p;
                }
            }
        }
        return closest;
    }

    public List<String> getRawLogMessagesDirectly() {
        List<String> currentMessages = new ArrayList<>(gameLogMessages);
        gameLogMessages.clear();
        return currentMessages;
    }

    private void handleSunDrop() {
        if (currentSeason != null && !currentSeason.allowsNaturalSunDrop()) {
            return;
        }
        SpecialLevelType specialType = level.getSpecialLevelType();
        if (specialType == SpecialLevelType.NIGHT_OPS || specialType == SpecialLevelType.PLANT_WHAT_YOU_GET || activeMiniGame != null) {
            return;
        }
        double t = tickCount / 10.0;
        double formulaInterval = Math.max(6 + 0.05 * t, 12);
        double scaleIncrease = difficultyLevel / 3.0;
        int sunDropInterval = (int) (formulaInterval * 10 * scaleIncrease);
        if (tickCount - lastSunDropTick >= sunDropInterval) {
            lastSunDropTick = tickCount;
            Random r = new Random();
            int x = r.nextInt(board.getColumns());
            int y = r.nextInt(board.getRows());
            int chance = r.nextInt(100);
            String sunType = "Normal";
            if (chance < 5) {
                sunType = "Radioactive";
                suns.add(new Sun(50, y, x));
            } else if (chance < 20) {
                sunType = "Special";
                suns.add(new Sun(100, y, x));
            } else {
                suns.add(new Sun(25, y, x));
            }
            gameLogMessages.add("New " + sunType + " sun is dropping at position (" + x + ", " + y + ")");
            gameLogMessages.add("Sun reached the ground at position (" + x + ", " + y + ")");
        }
    }

    public boolean hasZombieInRow(int row) {
        for (Zombie z : activeZombies) {
            if (z.getY() == row) return true;
        }
        return false;
    }

    public Zombie getFirstZombieInRowAhead(int row, double x) {
        Zombie closest = null;
        for (Zombie z : activeZombies) {
            if (z.getY() == row && z.getX() >= x) {
                if (closest == null || z.getX() < closest.getX()) {
                    closest = z;
                }
            }
        }
        return closest;
    }

    public Plant getPlantAt(int x, int y) {
        for (Plant p : activePlants) {
            if (p.getX() == x && p.getY() == y) return p;
        }
        return null;
    }

    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public LawnMower[] getLawnMowers() { return lawnMowers; }
    public boolean spendSun(int amount) {
        if (sunCount < amount) return false;
        sunCount -= amount;
        return true;
    }
    public void addSun(int amount) {
        sunCount += amount;
        sunsProducedInLevel += amount;
        scoreGame.onSunCollected(amount);
    }
    public boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }
    public void addCoins(int amount) {
        coins += amount;
        scoreGame.onCoinEarned(amount);
    }
    public boolean spendDiamonds(int amount) {
        if (diamonds < amount) return false;
        diamonds -= amount;
        return true;
    }
    public void addDiamonds(int amount) {
        diamonds += amount;
        scoreGame.onDiamondEarned(amount);
    }
    public void addPlantFood() { plantFoodCount++; }
    public boolean usePlantFood() {
        if (plantFoodCount <= 0) return false;
        plantFoodCount--;
        return true;
    }
    public void addBullet(Bullet bullet) { bullets.add(bullet); }
    public void addSun(Sun sun) { suns.add(sun); }
    public void addZombie(Zombie zombie) { activeZombies.add(zombie); }
    public void addPlant(Plant plant) {
        activePlants.add(plant);
        scoreGame.onPlantPlaced(plant);
    }
    public void removePlant(Plant plant) { activePlants.remove(plant); }
    public void removeZombie(Zombie zombie) {
        activeZombies.remove(zombie);
        scoreGame.onZombieKilled(zombie, this);
    }
    public boolean isRunning() { return running; }
    public Board getBoard() { return board; }
    public Level getLevel() { return level; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getSunCount() { return sunCount; }
    public int getCoins() { return coins; }
    public int getDiamonds() { return diamonds; }
    public int getPlantFoodCount() { return plantFoodCount; }
    public Spawner getSpawner() { return spawner; }
    public void setSpawner(Spawner spawner) { this.spawner = spawner; }
    public model.score.ScoreGame getScoreGame() { return scoreGame; }
    public Greenhouse getGreenhouse() { return greenhouse; }
    public void setGreenhouse(Greenhouse greenhouse) { this.greenhouse = greenhouse; }
    public List<Bullet> getBullets() { return bullets; }
    public List<Sun> getSuns() { return suns; }
    public List<Zombie> getActiveZombies() { return activeZombies; }
    public List<Plant> getActivePlants() { return activePlants; }
    public int getTickCount() { return tickCount; }
}
