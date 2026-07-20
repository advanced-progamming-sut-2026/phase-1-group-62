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
import model.quest.Quest;
import model.User;
import model.UserSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private Board board;
    private Level level;
    private Difficulty difficulty;
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
    private User currentUser;
    private int plantsPlacedInLevel;

    public Game() {
        this.board = new Board(5, 9);
        this.level = new Level(1);
        this.difficulty = Difficulty.NORMAL;
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
        this.currentUser = UserSession.getCurrentUser();
        this.plantsPlacedInLevel = 0;
    }

    public Game(int rows, int columns, int levelNumber, Difficulty difficulty) {
        this();
        this.board = new Board(rows, columns);
        this.level = new Level(levelNumber);
        this.difficulty = difficulty;
        this.spawner = new Spawner(board, levelNumber * 2, difficulty);
        this.lawnMowers = new LawnMower[rows];
        for (int i = 0; i < rows; i++) {
            lawnMowers[i] = new LawnMower(i);
        }
    }

    // --- Quest Notification Methods ---

    private void notifyQuestZombieKill(Zombie zombie) {
        if (currentUser == null) return;
        currentUser.updateQuestProgress(Quest.QuestType.DAILY, 1);
        currentUser.updateQuestProgress(Quest.QuestType.EPIC, 1);
        currentUser.updateQuestProgress(Quest.QuestType.STORY, 1);
        currentUser.updateQuestProgressByTitle("Chapter Hunter", 1);
        currentUser.updateQuestProgressByTitle("Professional Plant Slayer", 1);
        currentUser.updateQuestProgressByTitle("Only Cactus", 1);
        currentUser.updateQuestProgressByTitle("Quick Reflexes", 1);
        currentUser.updateQuestProgressByTitle("Family Slaughter", 1);
        currentUser.updateQuestProgressByTitle("Almost Victorious", 1);
        currentUser.updateQuestProgressByTitle("Lawn Mowing Time", 1);
        currentUser.updateQuestProgressByExactTitle("Pro Demolitionist", 1);
    }

    private void notifyQuestPlantPlaced(Plant plant) {
        if (currentUser == null) return;
        plantsPlacedInLevel++;
        if (plant.getCategory() != null && plant.getCategory().equalsIgnoreCase("EXPLOSIVE")) {
            currentUser.updateQuestProgressByExactTitle("Pro Demolitionist", 1);
        }
        // Check for sun producer limits
        if (plant.getCategory() != null && plant.getCategory().equalsIgnoreCase("SUN_PRODUCER")) {
            int sunProducers = 0;
            for (Plant p : activePlants) {
                if (p.getCategory() != null && p.getCategory().equalsIgnoreCase("SUN_PRODUCER")) {
                    sunProducers++;
                }
            }
            if (sunProducers > 3) {
                currentUser.updateQuestProgressByExactTitle("Cloudy Day", 1);
            }
        }
    }

    private void notifyQuestSunCollected(int amount) {
        if (currentUser == null) return;
        currentUser.updateQuestProgressByTitle("Daily Sun Collector", amount);
    }

    private void notifyQuestLevelWon() {
        if (currentUser == null) return;
        currentUser.updateQuestProgressByExactTitle("Economic Vegetarian", 1);
        currentUser.updateQuestProgressByExactTitle("Master of Defense", 1);
        currentUser.updateQuestProgressByExactTitle("Symmetry", 1);
        currentUser.updateQuestProgressByExactTitle("OCD Obsession", 1);
        currentUser.updateQuestProgressByExactTitle("Cloudy Day", 1);
        currentUser.updateQuestProgressByExactTitle("Empty Column", 1);
        currentUser.updateQuestProgressByExactTitle("Empty Row", 1);
        currentUser.updateQuestProgressByExactTitle("Defenseless Cross", 1);
        currentUser.updateQuestProgressByExactTitle("Flourish in Limitations", 1);
        currentUser.updateQuestProgressByExactTitle("Night or Day", 1);
        currentUser.updateQuestProgressByExactTitle("Win Streak", 1);
    }

    private void notifyQuestLawnMowerKill() {
        if (currentUser == null) return;
        currentUser.updateQuestProgressByTitle("Lawn Mowing Time", 1);
        currentUser.updateQuestProgressByTitle("Almost Victorious", 1);
    }

    private void notifyQuestLevelLost() {
        if (currentUser == null) return;
        // Reset win streak on loss
        for (Quest q : currentUser.getUserQuests()) {
            if (q.getTitle().equalsIgnoreCase("Win Streak") && q.getStatus() != Quest.QuestStatus.COMPLETED) {
                q.setProgress(0);
            }
        }
    }

    private void checkSpecialQuestConditions() {
        if (currentUser == null || !won) return;

        // Check Economic Vegetarian (plants lost <= n)
        Quest ecoQuest = findQuestByTitle("Economic Vegetarian");
        if (ecoQuest != null && ecoQuest.getStatus() != Quest.QuestStatus.COMPLETED) {
            if (plantsLostCount <= ecoQuest.getVariableN()) {
                ecoQuest.updateProgress(1);
            }
        }

        // Check Master of Defense (zero suns spent)
        Quest defenseQuest = findQuestByTitle("Master of Defense");
        if (defenseQuest != null && defenseQuest.getStatus() != Quest.QuestStatus.COMPLETED) {
            if (sunCount == 0) {
                defenseQuest.updateProgress(1);
            }
        }

        // Check Cloudy Day (max 3 sun producers)
        Quest cloudyQuest = findQuestByTitle("Cloudy Day");
        if (cloudyQuest != null && cloudyQuest.getStatus() != Quest.QuestStatus.COMPLETED) {
            int sunProducers = 0;
            for (Plant p : activePlants) {
                if (p.getCategory() != null && p.getCategory().equalsIgnoreCase("SUN_PRODUCER")) {
                    sunProducers++;
                }
            }
            if (sunProducers <= 3) {
                cloudyQuest.updateProgress(1);
            }
        }

        // Check Empty Column
        Quest emptyColQuest = findQuestByTitle("Empty Column");
        if (emptyColQuest != null && emptyColQuest.getStatus() != Quest.QuestStatus.COMPLETED) {
            int col = emptyColQuest.getVariableN();
            boolean empty = true;
            for (int r = 0; r < board.getRows(); r++) {
                if (getPlantAt(col, r) != null) {
                    empty = false;
                    break;
                }
            }
            if (empty) emptyColQuest.updateProgress(1);
        }

        // Check Empty Row
        Quest emptyRowQuest = findQuestByTitle("Empty Row");
        if (emptyRowQuest != null && emptyRowQuest.getStatus() != Quest.QuestStatus.COMPLETED) {
            int row = emptyRowQuest.getVariableN();
            boolean empty = true;
            for (int c = 0; c < board.getColumns(); c++) {
                if (getPlantAt(c, row) != null) {
                    empty = false;
                    break;
                }
            }
            if (empty) emptyRowQuest.updateProgress(1);
        }

        // Check Defenseless Cross
        Quest crossQuest = findQuestByTitle("Defenseless Cross");
        if (crossQuest != null && crossQuest.getStatus() != Quest.QuestStatus.COMPLETED) {
            int n = crossQuest.getVariableN();
            boolean empty = true;
            for (int r = 0; r < board.getRows(); r++) {
                if (getPlantAt(n, r) != null) { empty = false; break; }
            }
            for (int c = 0; c < board.getColumns(); c++) {
                if (getPlantAt(c, n) != null) { empty = false; break; }
            }
            if (empty) crossQuest.updateProgress(1);
        }
    }

    private Quest findQuestByTitle(String title) {
        if (currentUser == null) return null;
        for (Quest q : currentUser.getUserQuests()) {
            if (q.getTitle().equalsIgnoreCase(title)) {
                return q;
            }
        }
        return null;
    }

    // --- Start and Stop ---

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

    // --- Getters and Setters ---

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
        if (lost) notifyQuestLevelLost();
    }

    public void setWon(boolean won) {
        this.won = won;
        if (won) {
            checkSpecialQuestConditions();
            notifyQuestLevelWon();
        }
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

    // --- Game Loop ---

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
                return;
            }
            if (tickCount >= level.getTimeLimitTicks()) {
                lost = true;
                running = false;
                gameLogMessages.add("The zombie ate your brain; LOSER!!!");
                return;
            }
        }

        if (specialType == SpecialLevelType.DEAD_LINE || activeMiniGame instanceof WallnutBowling) {
            int lineCol = (activeMiniGame instanceof WallnutBowling) ? ((WallnutBowling) activeMiniGame).getDeadlineColumn() : level.getDeadlineColumn();
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
        for (Zombie zombie : new ArrayList<>(activeZombies)) {
            zombie.updateEffects();
            zombie.updateCooldown();

            if (!zombie.isAlive()) {
                zombiesToRemove.add(zombie);
                String deathMessage = "Zombie of type " + zombie.getName() + " is dead at (" + (int) Math.round(zombie.getX()) + ", " + zombie.getY() + ")";
                gameLogMessages.add(deathMessage);
                scoreGame.onZombieKilled(zombie, this);
                zombiesKilledInLevel++;
                notifyQuestZombieKill(zombie);
                continue;
            }

            if (!zombie.hasEffect(ZombieEffect.FROZEN)) {
                double zombieX = zombie.getX();
                int zombieY = zombie.getY();

                int targetPlantX = (int) Math.floor(zombieX);
                if (zombieX - targetPlantX == 0.0) {
                    targetPlantX = targetPlantX - 1;
                }

                Plant targetPlant = getPlantAt(targetPlantX, zombieY);

                if (targetPlant != null && !targetPlant.isBowlingBall() && zombieX - targetPlant.getX() <= 1.05) {
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
                                notifyQuestZombieKill(killed);
                                notifyQuestLawnMowerKill();
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

        if (lost || won || !running) return;

        if (spawner != null) {
            if ((specialType == SpecialLevelType.PLANT_WHAT_YOU_GET && !zombieWavesStarted) || activeMiniGame instanceof Vasebreaker || activeMiniGame instanceof IZombie || activeMiniGame instanceof Beghoul) {
            } else {
                Zombie newlySpawned = spawner.update();
                if (newlySpawned != null) {
                    activeZombies.add(newlySpawned);
                    int cost = model.entities.zombie.factory.ZombieFactory.getWaveCost(newlySpawned.getName());
                    gameLogMessages.add("Zombie " + newlySpawned.getName() + " spawned at wave " + spawner.getCurrentWave() + " in lane " + newlySpawned.getY() + " which costed " + cost + ".");
                }
            }
        }

        if (lost || won || !running) return;

        handleSunDrop();

        if (currentSeason != null) {
            currentSeason.handleTick(this);
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
            if (plant.isFrozen() || plant.isBowlingBall()) continue;
            if (plant.getCategory() != null && plant.getCategory().equalsIgnoreCase("SHOOTER")) {
                if (plant.shouldShoot() && hasZombieInRow(plant.getY())) {
                    bullets.add(new Bullet(20, plant.getY(), plant.getX() + 1));
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
        int sunDropInterval = (int) (formulaInterval * 10);

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

    // --- Helper Methods ---

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

    // --- Core Game Methods ---

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
        notifyQuestSunCollected(amount);
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
    
    public void addZombie(Zombie zombie) { 
        activeZombies.add(zombie); 
    }
    
    public void addPlant(Plant plant) { 
        activePlants.add(plant); 
        scoreGame.onPlantPlaced(plant);
        notifyQuestPlantPlaced(plant);
    }
    
    public void removePlant(Plant plant) { 
        activePlants.remove(plant); 
    }
    
    public void removeZombie(Zombie zombie) { 
        activeZombies.remove(zombie); 
        scoreGame.onZombieKilled(zombie, this);
        notifyQuestZombieKill(zombie);
    }
    
    public boolean isRunning() { return running; }
    public Board getBoard() { return board; }
    public Level getLevel() { return level; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
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
