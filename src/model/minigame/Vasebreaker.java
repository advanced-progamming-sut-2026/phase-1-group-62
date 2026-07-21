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

    // Vase types - ONLY 3 TYPES (NO EMPTY!)
    public static final String VASE_ZOMBIE = "zombie";
    public static final String VASE_GARGANTUAR = "gargantuar";
    public static final String VASE_PLANT = "plant";

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
        // COMPLETELY RESET EVERYTHING
        this.vaseContents = new String[rows][cols];
        this.vaseBroken = new boolean[rows][cols];
        this.totalVases = 0;
        this.brokenVasesCount = 0;
        this.stageLevel = level;
        this.isSetup = true;

        Random rand = new Random();

        // Columns 0,1,2 are EMPTY (no vases)
        // Columns 3-8 are FULLY FILLED with vases (5 rows x 6 columns = 30 vases)
        int minCol = 3;
        int maxCol = cols - 1;

        // Calculate how many of each type
        int totalPositions = rows * (maxCol - minCol + 1); // 5 * 6 = 30

        // Gargantuars: Level 1: 0, Level 2: 1, Level 3: 2
        int numGargantuar = level > 1 ? level - 1 : 0;
        // Plant vases: Level 1: 8, Level 2: 10, Level 3: 12
        int numPlantVases = 6 + level * 2;
        // Zombie vases: everything else
        int numZombieVases = totalPositions - numPlantVases - numGargantuar;

        // Create a list of all vase types
        List<String> vaseTypes = new ArrayList<>();

        // Add plant vases
        for (int i = 0; i < numPlantVases; i++) {
            vaseTypes.add(VASE_PLANT);
        }
        // Add zombie vases
        for (int i = 0; i < numZombieVases; i++) {
            vaseTypes.add(VASE_ZOMBIE);
        }
        // Add gargantuar vases
        for (int i = 0; i < numGargantuar; i++) {
            vaseTypes.add(VASE_GARGANTUAR);
        }

        // Shuffle the list randomly
        for (int i = vaseTypes.size() - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            String temp = vaseTypes.get(i);
            vaseTypes.set(i, vaseTypes.get(j));
            vaseTypes.set(j, temp);
        }

        // Place vases in columns 3-8
        int index = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                String content = vaseTypes.get(index);
                vaseContents[r][c] = content;
                totalVases++;
                index++;
            }
        }

        System.out.println("=== VASEBREAKER SETUP ===");
        System.out.println("Total vases: " + totalVases + " (ALL columns 3-8 filled)");
        System.out.println("Plant vases: " + numPlantVases);
        System.out.println("Zombie vases: " + numZombieVases);
        System.out.println("Gargantuar vases: " + numGargantuar);
        System.out.println("Columns 0,1,2 are EMPTY (no vases)");
        System.out.println("Columns 3-8 are FULLY FILLED with vases");
    }

    public String getVaseContent(int r, int c) {
        if (r >= 0 && r < vaseContents.length && c >= 0 && c < vaseContents[0].length) {
            return vaseContents[r][c];
        }
        return null;
    }

    public boolean isVaseBroken(int r, int c) {
        if (r >= 0 && r < vaseBroken.length && c >= 0 && c < vaseBroken[0].length) {
            return vaseBroken[r][c];
        }
        return true;
    }

    public void breakVase(int r, int c, Game game) {
        if (r < 0 || r >= vaseBroken.length || c < 0 || c >= vaseBroken[0].length) {
            System.out.println("Vasebreaker: Invalid coordinates (" + c + ", " + r + ")");
            return;
        }
        if (vaseBroken[r][c]) {
            System.out.println("Vasebreaker: Vase at (" + c + ", " + r + ") already broken");
            return;
        }

        vaseBroken[r][c] = true;
        brokenVasesCount++;

        String content = vaseContents[r][c];
        Tile tile = game.getBoard().getTile(r, c);

        // This should NEVER happen - every vase has content
        if (content == null) {
            System.out.println("Vasebreaker: ERROR - Vase at (" + c + ", " + r + ") has no content! This should not happen.");
            return;
        }

        if (content.equals(VASE_ZOMBIE)) {
            Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("NormalZombie", r, c);
            if (z == null) {
                z = new Zombie("NormalZombie", 200, 0.5, 20);
                z.setX(c);
                z.setY(r);
            }
            game.addZombie(z);
            tile.setZombie(z);
            System.out.println("Vasebreaker: ZOMBIE appeared at (" + c + ", " + r + ")!");
        } else if (content.equals(VASE_GARGANTUAR)) {
            Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("Gargantuar", r, c);
            if (z == null) {
                z = new Zombie("Gargantuar", 1800, 0.3, 100);
                z.setX(c);
                z.setY(r);
            }
            game.addZombie(z);
            tile.setZombie(z);
            System.out.println("Vasebreaker: GARGANTUAR appeared at (" + c + ", " + r + ")!");
        } else if (content.equals(VASE_PLANT)) {
            String plantType = PLANT_TYPES[new Random().nextInt(PLANT_TYPES.length)];
            tile.setTemporarySeedPacket(plantType);
            tile.setSeedPacketTimer(80);
            System.out.println("Vasebreaker: " + plantType + " SEED PACKET dropped at (" + c + ", " + r + ")!");
        }
    }

    public void pickupPacket(int r, int c, Game game) {
        Tile tile = game.getBoard().getTile(r, c);
        String packet = tile.getTemporarySeedPacket();
        if (packet == null) {
            System.out.println("Vasebreaker: No seed packet at (" + c + ", " + r + ")");
            return;
        }

        Plant p = PlantFactory.createPlant(packet);
        if (p == null) {
            System.out.println("Vasebreaker: Failed to create plant from seed packet");
            return;
        }

        tile.setTemporarySeedPacket(null);
        tile.setSeedPacketTimer(0);
        p.setX(c);
        p.setY(r);
        game.addPlant(p);
        tile.setPlant(p);
        System.out.println("Vasebreaker: Planted " + packet + " at (" + c + ", " + r + ")!");
    }

    public int getTotalVases() { return totalVases; }
    public int getBrokenVasesCount() { return brokenVasesCount; }
    public int getStageLevel() { return stageLevel; }
    public void setStageLevel(int level) { this.stageLevel = Math.min(level, maxStageLevel); }
    public boolean isSetup() { return isSetup; }
    public void setSetup(boolean setup) { isSetup = setup; }
    public void resetGrid() {
        this.vaseContents = new String[5][9];
        this.vaseBroken = new boolean[5][9];
        this.totalVases = 0;
        this.brokenVasesCount = 0;
        this.isSetup = false;
    }

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

    public void updateMiniGame(Game game) {
        // Setup grid if not done
        if (!isSetup) {
            setupVaseGrid(game.getBoard().getRows(), game.getBoard().getColumns(), stageLevel);
            // Remove any existing plants from board
            for (Plant p : new ArrayList<>(game.getActivePlants())) {
                game.getBoard().getTile(p.getY(), p.getX()).setPlant(null);
                game.removePlant(p);
            }
            // Remove any existing zombies
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
                game.removeZombie(z);
            }
            return;
        }

        // Update seed packet timers
        for (int r = 0; r < game.getBoard().getRows(); r++) {
            for (int c = 0; c < game.getBoard().getColumns(); c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile.getTemporarySeedPacket() != null) {
                    tile.setSeedPacketTimer(tile.getSeedPacketTimer() - 1);
                    if (tile.getSeedPacketTimer() <= 0) {
                        System.out.println("Vasebreaker: Seed Packet for " + tile.getTemporarySeedPacket() + " at (" + c + ", " + r + ") disappeared!");
                        tile.setTemporarySeedPacket(null);
                    }
                }
            }
        }

        // Check win condition
        if (isVictoryConditionMet() && game.getActiveZombies().isEmpty()) {
            completeLevel(stageLevel, brokenVasesCount);
            if (stageLevel < maxStageLevel) {
                System.out.println("Vasebreaker: Level " + stageLevel + " complete! Moving to Level " + (stageLevel + 1));
                stageLevel++;
                isSetup = false;
                // Reset board for next level
                for (Plant p : new ArrayList<>(game.getActivePlants())) {
                    game.getBoard().getTile(p.getY(), p.getX()).setPlant(null);
                    game.removePlant(p);
                }
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
                    game.removeZombie(z);
                }
                setupVaseGrid(game.getBoard().getRows(), game.getBoard().getColumns(), stageLevel);
                game.getGameLogMessages().add("Vasebreaker: New level started! Level " + stageLevel);
            } else {
                game.setWon(true);
                game.stop();
                System.out.println("Vasebreaker: All levels complete! Victory!");
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            }
            return;
        }

        // Check loss condition
        if (isLossConditionMet(game)) {
            game.setLost(true);
            game.stop();
            System.out.println("Vasebreaker: Game Over! A zombie reached the house!");
            game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
            return;
        }
    }
}
