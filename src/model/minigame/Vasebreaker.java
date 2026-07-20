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

    // Special vase types
    public static final String VASE_EMPTY = "empty";
    public static final String VASE_ZOMBIE = "zombie";
    public static final String VASE_GARGANTUAR = "gargantuar";
    public static final String VASE_PLANT = "plant";
    public static final String VASE_SPECIAL_PLANT = "special_plant";
    public static final String VASE_SUN = "sun";

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

        // Determine number of vases based on stage level
        int numVases = 8 + (level - 1) * 4; // Level 1: 8, Level 2: 12, Level 3: 16

        // Determine special vase count
        int numSpecial = Math.max(1, level); // Level 1: 1 special, Level 2: 2, Level 3: 3
        int numGargantuar = level > 1 ? 1 : 0; // Gargantuars only from level 2+

        // Place vases randomly
        for (int i = 0; i < numVases; i++) {
            int r, c;
            int attempts = 0;
            do {
                r = rand.nextInt(rows);
                c = rand.nextInt(cols);
                attempts++;
            } while ((vaseContents[r][c] != null || attempts < 50) && vaseContents[r][c] == null);

            // Determine content
            String content;
            int roll = rand.nextInt(100);

            // Special vases (Plant Vase with guaranteed seed)
            if (i < numSpecial) {
                content = VASE_SPECIAL_PLANT;
            }
            // Gargantuar vase
            else if (i < numSpecial + numGargantuar) {
                content = VASE_GARGANTUAR;
            }
            // Normal distribution
            else if (roll < 20) {
                content = VASE_EMPTY;
            } else if (roll < 60) {
                content = VASE_ZOMBIE;
            } else if (roll < 85) {
                content = VASE_PLANT;
            } else {
                content = VASE_SUN;
            }

            vaseContents[r][c] = content;
            totalVases++;
        }
    }

    public String getVaseContent(int r, int c) {
        if (r >= 0 && r < vaseContents.length && c >= 0 && c < vaseContents[0].length) {
            return vaseContents[r][c];
        }
        return null;
    }

    public void setVaseContent(int r, int c, String content) {
        if (r >= 0 && r < vaseContents.length && c >= 0 && c < vaseContents[0].length) {
            vaseContents[r][c] = content;
            totalVases++;
        }
    }

    public boolean isVaseBroken(int r, int c) {
        if (r >= 0 && r < vaseBroken.length && c >= 0 && c < vaseBroken[0].length) {
            return vaseBroken[r][c];
        }
        return true;
    }

    public void breakVase(int r, int c, Game game) {
    if (r < 0 || r >= vaseBroken.length || c < 0 || c >= vaseBroken[0].length) return;
    if (vaseBroken[r][c]) return;

    vaseBroken[r][c] = true;
    brokenVasesCount++;

    String content = vaseContents[r][c];
    Tile tile = game.getBoard().getTile(r, c);

    if (content == null || content.equals(VASE_EMPTY)) {
        System.out.println("Vasebreaker: Smashed empty vase at (" + c + ", " + r + ").");
    } else if (content.equals(VASE_ZOMBIE)) {
        Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("NormalZombie", r, c);
        if (z == null) {
            z = new Zombie("NormalZombie", 200, 0.5, 20);
            z.setX(c);
            z.setY(r);
        }
        game.addZombie(z);
        tile.setZombie(z);
        System.out.println("Vasebreaker: A Zombie appeared from the vase at (" + c + ", " + r + ")!");
    } else if (content.equals(VASE_GARGANTUAR)) {
        Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("Gargantuar", r, c);
        if (z == null) {
            z = new Zombie("Gargantuar", 1800, 0.3, 100);
            z.setX(c);
            z.setY(r);
            // setBoss removed - use a different approach
            // Gargantuars are identified by name
        }
        game.addZombie(z);
        tile.setZombie(z);
        System.out.println("Vasebreaker: A GARGANTUAR appeared from the special vase at (" + c + ", " + r + ")!");
    } else if (content.equals(VASE_PLANT) || content.equals(VASE_SPECIAL_PLANT)) {
        String plantType = PLANT_TYPES[new Random().nextInt(PLANT_TYPES.length)];
        tile.setTemporarySeedPacket(plantType);
        tile.setSeedPacketTimer(80);
        System.out.println("Vasebreaker: Dropped " + plantType + " Seed Packet at (" + c + ", " + r + ")! Pick it up quickly!");
    } else if (content.equals(VASE_SUN)) {
        game.addSun(50);
        System.out.println("Vasebreaker: Found 50 suns in the vase at (" + c + ", " + r + ")!");
    }
}

    public void pickupPacket(int r, int c, Game game) {
        Tile tile = game.getBoard().getTile(r, c);
        String packet = tile.getTemporarySeedPacket();
        if (packet == null) {
            System.out.println("Vasebreaker: No seed packet at (" + c + ", " + r + ").");
            return;
        }

        Plant p = PlantFactory.createPlant(packet);
        if (p == null) {
            System.out.println("Vasebreaker: Failed to create plant from seed packet.");
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
            // Complete level
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

        // Spawn occasional zombies from remaining vases if too many are broken
        if (brokenVasesCount > totalVases / 2 && game.getActiveZombies().size() < 3 && stageLevel > 1) {
            Random rand = new Random();
            if (rand.nextInt(100) < 10) {
                // Find an unbroken vase with zombie content
                for (int r = 0; r < vaseContents.length; r++) {
                    for (int c = 0; c < vaseContents[0].length; c++) {
                        if (!vaseBroken[r][c] && VASE_ZOMBIE.equals(vaseContents[r][c])) {
                            breakVase(r, c, game);
                            return;
                        }
                    }
                }
            }
        }
    }
}
