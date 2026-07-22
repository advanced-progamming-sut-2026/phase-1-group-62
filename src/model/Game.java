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

        for (Plant plant : new ArrayList<>(activePlants)) {
            if (plant.isFrozen() || plant.isBowlingBall()) continue;
            plant.update();
            if (plant.getCategory() != null && plant.getCategory().equalsIgnoreCase("SUN_PRODUCER")) {
                int plantInterval = (int) (plant.getActionInterval() * 10);
                if (plantInterval > 0 && plant.shouldShoot()) {
                    if (!plant.isHasSunToCollect()) {
                        plant.setHasSunToCollect(true);
                        gameLogMessages.add("plant " + plant.getName().toLowerCase() + " produced a sun at (" + plant.getX() + ", " + plant.getY() + ")");
                    }
                }
            }
        }

        List<Zombie> zombiesToRemove = new ArrayList<>();
        List<Zombie> zombiesToAdd = new ArrayList<>();

        for (Zombie zombie : new ArrayList<>(activeZombies)) {
            zombie.updateEffects();
            zombie.updateCooldown();

            if (!zombie.isAlive()) {
                zombiesToRemove.add(zombie);
                if (zombie.getStolenSuns() > 0) {
                    int returnSun = zombie.getName().equalsIgnoreCase("ZombieCrystalSkull") ? zombie.getStolenSuns() / 2 : zombie.getStolenSuns();
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

        for (Plant plant : activePlants) {
            if (plant.isFrozen() || plant.isBowlingBall() || plant.isTransformedToSheep()) continue;
            if (plant.getCategory() != null && plant.getCategory().equalsIgnoreCase("SHOOTER")) {
                if (plant.shouldShoot() && (hasZombieInRow(plant.getY()) || board.hasGraveInRow(plant.getY()))) {
                    Bullet.BulletType bType = Bullet.BulletType.NORMAL;
                    if (plant.getName().toLowerCase().contains("pult") || plant.getName().toLowerCase().contains("melon")) {
                        bType = Bullet.BulletType.LOB;
                    } else if (plant.getName().toLowerCase().contains("snow") || plant.getName().toLowerCase().contains("ice")) {
                        bType = Bullet.BulletType.ICE;
                    } else if (plant.getName().toLowerCase().contains("fire")) {
                        bType = Bullet.BulletType.FIRE;
                    }
                    bullets.add(new Bullet(20, plant.getY(), plant.getX() + 1, bType, false, false, 0));
                }
            }
        }

        board.updateProjectilesAndCollisions(this);

        if (spawner != null && !(activeMiniGame instanceof Vasebreaker) && !(activeMiniGame instanceof IZombie) && !(activeMiniGame instanceof Beghoul)) {
            if (spawner.isWaveComplete() && activeZombies.isEmpty()) {
                if (spawner.getCurrentWave() < spawner.getTotalWaves()) {
                    int nextWave = spawner.getCurrentWave() + 1;
                    if (currentSeason != null) {
                        currentSeason.handleWaveStart(this);
                    }
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
                        if (currentSeason != null) {
                            currentSeason.handleWaveStart(this);
                        }
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
    public void addSun(int amount) { sunCount += amount; sunsProducedInLevel += amount; scoreGame.onSunCollected(amount); }
    public boolean spendCoins(int amount) { if (coins < amount) return false; coins -= amount; return true; }
    public void addCoins(int amount) { coins += amount; scoreGame.onCoinEarned(amount); }
    public boolean spendDiamonds(int amount) { if (diamonds < amount) return false; diamonds -= amount; return true; }
    public void addDiamonds(int amount) { diamonds += amount; scoreGame.onDiamondEarned(amount); }
    public void addPlantFood() { plantFoodCount++; }
    public boolean usePlantFood() { if (plantFoodCount <= 0) return false; plantFoodCount--; return true; }
    public void addBullet(Bullet bullet) { bullets.add(bullet); }
    public void addSun(Sun sun) { suns.add(sun); }
    public void addZombie(Zombie zombie) { activeZombies.add(zombie); }
    public void addPlant(Plant plant) { activePlants.add(plant); scoreGame.onPlantPlaced(plant); }
    public void removePlant(Plant plant) { activePlants.remove(plant); }
    public void removeZombie(Zombie zombie) { activeZombies.remove(zombie); scoreGame.onZombieKilled(zombie, this); }
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