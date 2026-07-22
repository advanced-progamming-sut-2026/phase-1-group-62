package model.minigame;

import model.Game;
import model.Tile;
import model.entities.zombie.Zombie;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Vasebreaker extends MiniGame {
    private String[][] vaseContents;
    private boolean[][] vaseBroken;
    private int totalVases;
    private int brokenVasesCount;
    private int stageLevel;
    private int maxStageLevel;
    private boolean isSetup;

    public static final String VASE_EMPTY = "empty";
    public static final String VASE_ZOMBIE = "zombie";
    public static final String VASE_GARGANTUAR = "gargantuar";
    public static final String VASE_PLANT = "plant";
    public static final String VASE_SPECIAL_PLANT = "special_plant";
    public static final String VASE_SUN = "sun";
    public static final String VASE_REGULAR = "regular";

    private static final String[] PLANT_TYPES = {"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Repeater"};

    public Vasebreaker() {
        super("Vasebreaker");
        this.vaseContents = new String[5][9];
        this.vaseBroken = new boolean[5][9];
        this.totalVases = 0;
        this.brokenVasesCount = 0;
        this.stageLevel = 1;
        this.maxStageLevel = 3;
        this.isSetup = false;
    }

    public void setupVaseGrid(int rows, int cols, int level) {
        this.stageLevel = level;
        this.vaseContents = new String[rows][cols];
        this.vaseBroken = new boolean[rows][cols];
        this.totalVases = 0;
        this.brokenVasesCount = 0;
        this.isSetup = true;

        Random rand = new Random();

        int numVases;
        if (level == 1) {
            numVases = 6;
        } else if (level == 2) {
            numVases = 12;
        } else {
            numVases = 16;
        }

        int numPlantVases = Math.max(2, 5 - level);
        int numGargantuarVases = level;

        for (int i = 0; i < numPlantVases; i++) {
            placeVaseRandomly(rows, cols, VASE_PLANT, rand);
        }

        for (int i = 0; i < numGargantuarVases; i++) {
            placeVaseRandomly(rows, cols, VASE_GARGANTUAR, rand);
        }

        while (totalVases < numVases) {
            placeVaseRandomly(rows, cols, VASE_REGULAR, rand);
        }
    }

    private void placeVaseRandomly(int rows, int cols, String type, Random rand) {
        int r, c;
        int attempts = 0;
        do {
            r = rand.nextInt(rows);
            c = rand.nextInt(3, cols);
            attempts++;
        } while (vaseContents[r][c] != null && attempts < 300);

        if (vaseContents[r][c] == null) {
            vaseContents[r][c] = type;
            totalVases++;
        }
    }

    public boolean hasVase(int r, int c) {
        if (r >= 0 && r < vaseContents.length && c >= 0 && c < vaseContents[0].length) {
            return vaseContents[r][c] != null;
        }
        return false;
    }

    public String getVaseContent(int r, int c) {
        if (r >= 0 && r < vaseContents.length && c >= 0 && c < vaseContents[0].length) {
            return vaseContents[r][c];
        }
        return null;
    }

    public boolean isVaseBroken(int r, int c) {
        if (r >= 0 && r < vaseBroken.length && c >= 0 && c < vaseBroken[0].length) {
            if (vaseContents[r][c] == null) return true;
            return vaseBroken[r][c];
        }
        return true;
    }

    public void breakVase(int r, int c, Game game) {
        if (r < 0 || r >= vaseBroken.length || c < 0 || c >= vaseBroken[0].length) return;
        if (vaseBroken[r][c] || vaseContents[r][c] == null) return;

        vaseBroken[r][c] = true;
        brokenVasesCount++;

        String content = vaseContents[r][c];
        Tile tile = game.getBoard().getTile(r, c);
        Random rand = new Random();

        if (content.equals(VASE_REGULAR)) {
            int roll = rand.nextInt(100);
            if (roll < 20) {
                content = VASE_EMPTY;
            } else if (roll < 55) {
                content = VASE_ZOMBIE;
            } else if (roll < 80) {
                content = VASE_PLANT;
            } else {
                content = VASE_SUN;
            }
        }

        if (content.equals(VASE_EMPTY)) {
            game.getGameLogMessages().add("Vasebreaker: Smashed empty vase at (" + c + ", " + r + ").");
        } else if (content.equals(VASE_ZOMBIE)) {
            Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("NormalZombie", r, c);
            if (z == null) {
                z = new Zombie("NormalZombie", 200, 0.5, 20);
                z.setX(c);
                z.setY(r);
            }
            game.addZombie(z);
            tile.setZombie(z);
            game.getGameLogMessages().add("Vasebreaker: A Zombie appeared from the vase at (" + c + ", " + r + ")!");
        } else if (content.equals(VASE_GARGANTUAR)) {
            Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("ZombieGargantuar", r, c);
            if (z == null) {
                z = new Zombie("ZombieGargantuar", 1800, 0.3, 100);
                z.setX(c);
                z.setY(r);
            }
            game.addZombie(z);
            tile.setZombie(z);
            game.getGameLogMessages().add("Vasebreaker: A GARGANTUAR appeared from the special vase at (" + c + ", " + r + ")!");
        } else if (content.equals(VASE_PLANT) || content.equals(VASE_SPECIAL_PLANT)) {
            String plantType = PLANT_TYPES[rand.nextInt(PLANT_TYPES.length)];
            tile.setTemporarySeedPacket(plantType);
            tile.setSeedPacketTimer(100);
            game.getGameLogMessages().add("Vasebreaker: Dropped " + plantType + " Seed Packet at (" + c + ", " + r + ")! Pick it up quickly!");
        } else if (content.equals(VASE_SUN)) {
            game.addSun(50);
            game.getGameLogMessages().add("Vasebreaker: Found 50 suns in the vase at (" + c + ", " + r + ")!");
        }
    }

    public int getTotalVases() { return totalVases; }
    public int getBrokenVasesCount() { return brokenVasesCount; }
    public int getStageLevel() { return stageLevel; }

    public boolean isVictoryConditionMet() {
        return brokenVasesCount >= totalVases && totalVases > 0;
    }

    public boolean isLossConditionMet(Game game) {
        for (Zombie z : game.getActiveZombies()) {
            if (z.getX() <= 0) {
                return true;
            }
        }
        return false;
    }

    private void clearBoard(Game game) {
        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            Tile tile = game.getBoard().getTile(p.getY(), p.getX());
            if (tile != null) {
                tile.setPlant(null);
            }
            game.removePlant(p);
        }
        for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
            Tile tile = game.getBoard().getTile(z.getY(), (int) z.getX());
            if (tile != null) {
                tile.setZombie(null);
            }
            game.removeZombie(z);
        }
        for (int r = 0; r < game.getBoard().getRows(); r++) {
            for (int c = 0; c < game.getBoard().getColumns(); c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile != null) {
                    tile.setTemporarySeedPacket(null);
                    tile.setSeedPacketTimer(0);
                }
            }
        }
    }

    public void updateMiniGame(Game game) {
        if (!isSetup) {
            clearBoard(game);
            setupVaseGrid(game.getBoard().getRows(), game.getBoard().getColumns(), stageLevel);
        }

        for (int r = 0; r < game.getBoard().getRows(); r++) {
            for (int c = 0; c < game.getBoard().getColumns(); c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile.getTemporarySeedPacket() != null) {
                    tile.setSeedPacketTimer(tile.getSeedPacketTimer() - 1);
                    if (tile.getSeedPacketTimer() <= 0) {
                        game.getGameLogMessages().add("Vasebreaker: Seed Packet for " + tile.getTemporarySeedPacket() + " at (" + c + ", " + r + ") expired!");
                        tile.setTemporarySeedPacket(null);
                    }
                }
            }
        }

        if (isVictoryConditionMet() && game.getActiveZombies().isEmpty()) {
            completeLevel(stageLevel, brokenVasesCount);
            if (stageLevel < maxStageLevel) {
                stageLevel++;
                isSetup = false;
                clearBoard(game);
                setupVaseGrid(game.getBoard().getRows(), game.getBoard().getColumns(), stageLevel);
                game.getGameLogMessages().add("Vasebreaker: Stage " + (stageLevel - 1) + " Complete! Advanced to Stage " + stageLevel);
            } else {
                game.setWon(true);
                game.stop();
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
            return;
        }

        if (isLossConditionMet(game)) {
            game.setLost(true);
            game.stop();
            game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
        }
    }
}